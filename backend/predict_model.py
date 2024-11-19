import pandas as pd
import pickle
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error
from flask import Flask, request, jsonify
from mysql.connector import pooling
import traceback
import mysql.connector

app = Flask(__name__)

db_config = {
    "host": "localhost",
    "user": "oduwairi",
    "password": "osama.dwairi21",
    "database": "shoppingappdb",
    "connection_timeout": 10
}

connection_pool = pooling.MySQLConnectionPool(
    pool_name="mypool",
    pool_size=15,
    **db_config
)

def get_db_connection():
    return connection_pool.get_connection()

@app.route('/train_model', methods=['POST'])
def train_model():
    try:
        # Connect to the database
        db = get_db_connection()
        query = """
            SELECT stocked_at AS purchase_date, quantity_stocked AS quantity, price, priority, category_id
            FROM InventoryItems
        """
        data = pd.read_sql(query, db)
        db.close()

        # Feature Engineering
        data['purchase_date'] = pd.to_datetime(data['purchase_date'])
        data['purchase_interval'] = data['purchase_date'].diff().dt.days
        data = data.dropna()

        # Check if there's enough data to train
        if data.empty:
            return jsonify({"error": "Not enough data to train the model"}), 400

        # One-Hot Encode `category_id`
        data = pd.get_dummies(data, columns=['category_id'], prefix='category', drop_first=True)

        # Define features and target
        features = ['quantity', 'price', 'priority'] + [col for col in data.columns if col.startswith('category_')]
        target = 'purchase_interval'
        X = data[features]
        y = data[target]

        # Train/Test Split
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

        # Train the model
        model = LinearRegression()
        model.fit(X_train, y_train)

        # Evaluate the model
        predictions = model.predict(X_test)
        mae = mean_absolute_error(y_test, predictions)

        # Save the model and features
        with open('regression_model.pkl', 'wb') as f:
            pickle.dump(model, f)

        # Save feature names for prediction consistency
        with open('model_features.pkl', 'wb') as f:
            pickle.dump(features, f)

        return jsonify({"message": "Model trained successfully!", "MAE": mae}), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route('/predict_interval', methods=['POST'])
def predict_interval():
    try:
        # Load the trained model and feature names
        with open('regression_model.pkl', 'rb') as f:
            model = pickle.load(f)
        with open('model_features.pkl', 'rb') as f:
            feature_names = pickle.load(f)

        # Get input data from the request
        data = request.json
        last_purchase_date = pd.to_datetime(data.get('last_purchase_date'))
        days_since_last_purchase = (pd.Timestamp.now() - last_purchase_date).days

        # Validate input
        category_id = data.get('category_id', None)
        if category_id is None:
            return jsonify({"error": "category_id is required"}), 400

        # Prepare features
        input_features = {
            'quantity': data.get('quantity', 0),
            'price': data.get('price', 0),
            'priority': data.get('priority', 0)
        }

        # Handle one-hot encoding for category_id
        for feature in feature_names:
            if feature.startswith('category_'):
                input_features[feature] = 1 if feature == f'category_{category_id}' else 0

        # Convert input to DataFrame
        features_df = pd.DataFrame([input_features])[feature_names]

        # Predict interval
        predicted_interval = model.predict(features_df)[0]

        # Handle invalid predictions
        if predicted_interval <= 0:
            return jsonify({"error": "Predicted interval is invalid"}), 400

        # Calculate restock date
        restock_date = last_purchase_date + pd.to_timedelta(predicted_interval, unit='D')

        # Calculate frequency value
        frequency_value = predicted_interval 

        # Return the result
        return jsonify({
            "predicted_interval": predicted_interval,
            "restock_date": restock_date.strftime('%Y-%m-%d'),
            "frequency_value": frequency_value
        }), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(debug=True)
