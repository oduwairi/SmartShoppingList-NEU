from flask import Flask, jsonify, request
from flask_cors import CORS
from mysql.connector import pooling
import traceback
import mysql.connector
import pandas as pd
import pickle
from xgboost import XGBRegressor
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
                existing_item.get('price', None),
                existing_item.get('quantity_stocked', None),
                existing_item.get('priority', 5)
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
    
@app.route('/shopping_items/<int:item_id>', methods=['PUT'])
def update_shopping_item(item_id):
    try:
        data = request.json  # Get the JSON data from the request body
        
        # Extract fields from the request data
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

        # Connect to the database
        db = get_db_connection()
        cursor = db.cursor()

        # Update query
        query = """
            UPDATE ShoppingItems
            SET
                list_id = %s,
                item_name = %s,
                quantity = %s,
                quantity_unit = %s,
                price = %s,
                currency = %s,
                image_url = %s,
                priority = %s,
                frequency_value = %s,
                frequency_unit = %s,
                category_id = %s
            WHERE item_id = %s
        """
        
        # Execute the query with parameters
        cursor.execute(query, (
            list_id, item_name, quantity, quantity_unit, price, currency,
            image_url, priority, frequency_value, frequency_unit, category_id, item_id
        ))
        db.commit()

        # Check if the update was successful
        if cursor.rowcount > 0:
            cursor.close()
            db.close()
            return jsonify({"message": f"Shopping item {item_id} updated successfully!"}), 200
        else:
            cursor.close()
            db.close()
            return jsonify({"error": "Item not found"}), 404

    except Exception as e:
        traceback.print_exc()  # Print the stack trace in the server log
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
            SELECT 
                restock_history, 
                price_history, 
                quantity_history, 
                priority_history 
            FROM inventory_item_history
        """
        data = pd.read_sql(query, db)
        db.close()

        # Convert restock_history to datetime and calculate intervals
        data['restock_history'] = pd.to_datetime(data['restock_history'])
        data['restock_interval'] = data['restock_history'].diff().dt.seconds / 60  # Interval in minutes

        # Drop rows with missing restock intervals
        data = data.dropna(subset=['restock_interval'])

        # Check if there's enough data
        if len(data) < 2:  # At least 2 rows needed
            return jsonify({"error": "Not enough valid historical data to train the model"}), 400

        # Prepare features
        data['lag_1'] = data['restock_interval'].shift(1).fillna(0)
        data['lag_2'] = data['restock_interval'].shift(2).fillna(0)
        features = ['lag_1', 'lag_2', 'price_history', 'quantity_history', 'priority_history']
        data = data.dropna()

        X = data[features]
        y = data['restock_interval']

        # Train on the full dataset (no train-test split)
        model = XGBRegressor(
            n_estimators=50,  # Reduce trees for small data
            max_depth=3,      # Limit tree depth
            learning_rate=0.1,
            reg_alpha=0.1,    # L1 regularization
            reg_lambda=1.0,   # L2 regularization
            random_state=42
        )
        model.fit(X, y)

        # Save the model and feature names
        with open('xgboost_model.pkl', 'wb') as f:
            pickle.dump(model, f)

        with open('model_features.pkl', 'wb') as f:
            pickle.dump(features, f)

        return jsonify({"message": "Model trained successfully!"}), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500



@app.route('/predict_interval', methods=['POST'])
def predict_interval():
    try:
        # Load the trained model and feature list
        with open('xgboost_model.pkl', 'rb') as f:
            model = pickle.load(f)

        with open('model_features.pkl', 'rb') as f:
            feature_names = pickle.load(f)

        # Get input data from the request
        data = request.json
        item_id = data.get('item_id')

        # Fetch all historical data for the given item_id
        db = get_db_connection()
        query = """
            SELECT 
                restock_history, 
                price_history, 
                quantity_history, 
                priority_history 
            FROM inventory_item_history
            WHERE item_id = %s
            ORDER BY restock_history ASC
        """
        historical_data = pd.read_sql(query, db, params=(item_id,))
        db.close()

        if historical_data.empty:
            return jsonify({"error": "No historical data found for the given item_id"}), 400

        # Preprocess historical data
        historical_data['restock_history'] = pd.to_datetime(historical_data['restock_history'])
        historical_data['restock_interval'] = historical_data['restock_history'].diff().dt.seconds / 60

        # Handle small datasets
        historical_data['lag_1'] = historical_data['restock_interval'].shift(1).fillna(0)
        historical_data['lag_2'] = historical_data['restock_interval'].shift(2).fillna(0)

        # Use the most recent data point
        recent_data = historical_data.iloc[-1]

        # Prepare features for prediction
        features = {
            'lag_1': recent_data['lag_1'],
            'lag_2': recent_data['lag_2'],
            'price_history': recent_data['price_history'],
            'quantity_history': recent_data['quantity_history'],
            'priority_history': recent_data['priority_history']
        }

        # Ensure all features match the model
        features_df = pd.DataFrame([features])[feature_names]

        # Predict interval
        predicted_interval = model.predict(features_df)[0]

        # Convert to float for JSON serialization
        predicted_interval = float(predicted_interval)

        return jsonify({
            "predicted_interval": predicted_interval
        }), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500



# Run the Flask server
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
