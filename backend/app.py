from flask import Flask, jsonify, request
from flask_cors import CORS
from mysql.connector import pooling
import traceback
import mysql.connector
import pandas as pd
import pickle
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error

app = Flask(__name__)
CORS(app)  # Enable CORS to allow requests from your Android app

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

@app.route('/categories', methods=['GET'])
def get_categories():
    try:
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        cursor.execute("SELECT * FROM categories")
        categories = cursor.fetchall()
        cursor.close()
        db.close()
        return jsonify(categories), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/categories', methods=['POST'])
def add_category():
    try:
        data = request.json
        category_name = data.get('category_name')
        category_image_url = data.get('category_image_url')
        category_color = data.get('category_color')
        
        db = get_db_connection()
        cursor = db.cursor()
        query = "INSERT INTO Categories (category_name, category_image_url, category_color) VALUES (%s, %s, %s)"
        cursor.execute(query, (category_name, category_image_url, category_color))
        db.commit()
        cursor.close()
        db.close()
        return jsonify({"message": "Category added successfully!"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/inventory_items', methods=['GET'])
def get_inventory_items():
    try:
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        query = "SELECT * FROM InventoryItems"
        cursor.execute(query)
        result = cursor.fetchall()
        cursor.close()
        db.close()
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/inventory_items', methods=['POST'])
def add_inventory_item():
    try:
        data = request.json
        inventory_id = data.get('inventory_id')
        item_id = data.get('item_id')
        item_name = data.get('item_name')
        quantity_stocked = data.get('quantity_stocked')
        quantity_unit = data.get('quantity_unit')
        price = data.get('price')
        currency = data.get('currency')
        image_url = data.get('image_url')
        priority = data.get('priority')
        category_id = data.get('category_id')
        stocked_at = data.get('stocked_at')
        restock_date = data.get('restock_date')

        db = get_db_connection()
        cursor = db.cursor(dictionary=True)

        # Check if the item exists using item_name and category_id for uniqueness
        check_query = "SELECT * FROM InventoryItems WHERE LOWER(item_name) = %s AND category_id = %s"
        cursor.execute(check_query, (item_name.lower(), category_id))
        existing_item = cursor.fetchone()

        # If the item exists, log the update in the history table
        if existing_item:
            history_query = """
                INSERT INTO inventory_item_history (
                    item_id, restock_history, price_history, quantity_history, priority_history
                ) VALUES (%s, %s, %s, %s, %s)
            """
            cursor.execute(history_query, (
                existing_item['item_id'],
                existing_item.get('stocked_at', None),
                existing_item.get('price', 0),
                existing_item.get('quantity_stocked', 0),
                existing_item.get('priority', 1)
            ))

        # Insert or update the inventory item
        query = """
            INSERT INTO InventoryItems (
                inventory_id, item_id, item_name, quantity_stocked, quantity_unit, price, currency,
                image_url, priority, category_id, stocked_at, restock_date
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                quantity_stocked = VALUES(quantity_stocked),
                quantity_unit = VALUES(quantity_unit),
                price = VALUES(price),
                currency = VALUES(currency),
                image_url = VALUES(image_url),
                priority = VALUES(priority),
                category_id = VALUES(category_id),
                stocked_at = VALUES(stocked_at),
                restock_date = VALUES(restock_date)
        """
        cursor.execute(query, (
            inventory_id, item_id, item_name, quantity_stocked, quantity_unit, price, currency,
            image_url, priority, category_id, stocked_at, restock_date
        ))

        db.commit()
        cursor.close()
        db.close()

        return jsonify({"message": "Inventory item added or updated successfully!"}), 201

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500



@app.route('/shopping_items', methods=['GET'])
def get_shopping_items():
    try:
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        query = "SELECT * FROM ShoppingItems"
        cursor.execute(query)
        result = cursor.fetchall()
        cursor.close()
        db.close()
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/shopping_items', methods=['POST'])
def add_shopping_item():
    try:
        data = request.json
        list_id = data.get('list_id')
        item_name = data.get('item_name')
        quantity = data.get('quantity')
        quantity_unit = data.get('quantity_unit')
        price = data.get('price')
        currency = data.get('currency')
        image_url = data.get('image_url')
        priority = data.get('priority')
        frequency_value = data.get('frequency_value')
        frequency_unit = data.get('frequency_unit')
        category_id = data.get('category_id')
        created_at = data.get('added_at')

        db = get_db_connection()
        cursor = db.cursor()
        
        # Insert or update the shopping item
        query = """
            INSERT INTO ShoppingItems (
                list_id, item_name, quantity, quantity_unit, price, currency,
                image_url, priority, frequency_value, frequency_unit, category_id, created_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                quantity = VALUES(quantity),
                quantity_unit = VALUES(quantity_unit),
                price = VALUES(price),
                currency = VALUES(currency),
                image_url = VALUES(image_url),
                priority = VALUES(priority),
                frequency_value = VALUES(frequency_value),
                frequency_unit = VALUES(frequency_unit),
                category_id = VALUES(category_id),
                created_at = VALUES(created_at)
        """
        
        cursor.execute(query, (
            list_id, item_name, quantity, quantity_unit, price, currency,
            image_url, priority, frequency_value, frequency_unit, category_id, created_at
        ))
        db.commit()
        cursor.close()
        db.close()
        
        return jsonify({"message": "Shopping item added or updated successfully!"}), 201
    except Exception as e:
        traceback.print_exc()  # Print the stack trace in the server log
        return jsonify({"error": str(e)}), 500


@app.route('/shopping_items/<int:item_id>', methods=['DELETE'])
def delete_shopping_item(item_id):
    try:
        db = get_db_connection()
        cursor = db.cursor()
        query = "DELETE FROM ShoppingItems WHERE item_id = %s"
        cursor.execute(query, (item_id,))
        db.commit()
        if cursor.rowcount > 0:
            cursor.close()
            db.close()
            return jsonify({"message": f"Shopping item {item_id} deleted successfully!"}), 200
        else:
            cursor.close()
            db.close()
            return jsonify({"error": "Item not found"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
@app.route('/predefined_items', methods=['GET'])
def get_predefined_items():
    try:
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        query = "SELECT * FROM predefined_items"
        cursor.execute(query)
        result = cursor.fetchall()
        cursor.close()
        db.close()
        return jsonify(result), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
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

# Run the Flask server
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
