from flask import Flask, jsonify, request
from flask_cors import CORS
from mysql.connector import pooling
import traceback
import mysql.connector

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
        cursor = db.cursor()
        query = """
            INSERT INTO InventoryItems (
                inventory_id, item_name, quantity_stocked, quantity_unit, price, currency,
                image_url, priority, category_id, stocked_at, restock_date
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        cursor.execute(query, (
            inventory_id, item_name, quantity_stocked, quantity_unit, price, currency,
            image_url, priority, category_id, stocked_at, restock_date
        ))
        db.commit()
        cursor.close()
        db.close()
        return jsonify({"message": "Inventory item added successfully!"}), 201
    except Exception as e:
        traceback.print_exc()  # Print the stack trace in the server log
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
        query = """
            INSERT INTO ShoppingItems (
                list_id, item_name, quantity, quantity_unit, price, currency,
                image_url, priority, frequency_value, frequency_unit, category_id, created_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        cursor.execute(query, (
            list_id, item_name, quantity, quantity_unit, price, currency,
            image_url, priority, frequency_value, frequency_unit, category_id, created_at
        ))
        db.commit()
        cursor.close()
        db.close()
        return jsonify({"message": "Shopping item added successfully!"}), 201
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

# Run the Flask server
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
