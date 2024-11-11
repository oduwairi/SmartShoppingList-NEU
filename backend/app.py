from flask import Flask, jsonify, request
from flask_cors import CORS
import mysql.connector

app = Flask(__name__)
CORS(app)  # Enable CORS to allow requests from your Android app

db = mysql.connector.connect(
    host="localhost",
    user="oduwairi",
    password="osama.dwairi21",
    database="shoppingappdb"
)

@app.route('/categories', methods=['GET'])
def get_categories():
    cursor = db.cursor(dictionary=True)
    query = "SELECT * FROM Categories"
    cursor.execute(query)
    result = cursor.fetchall()
    cursor.close()
    return jsonify(result)

@app.route('/categories', methods=['POST'])
def add_category():
    data = request.json
    category_name = data.get('category_name')
    category_image_url = data.get('category_image_url')
    category_color = data.get('category_color')

    cursor = db.cursor()
    query = "INSERT INTO Categories (category_name, category_image_url, category_color) VALUES (%s, %s, %s)"
    cursor.execute(query, (category_name, category_image_url, category_color))
    db.commit()
    cursor.close()
    return jsonify({"message": "Category added successfully!"}), 201

@app.route('/inventory_items', methods=['GET'])
def get_inventory_items():
    cursor = db.cursor(dictionary=True)
    query = "SELECT * FROM InventoryItems"
    cursor.execute(query)
    result = cursor.fetchall()
    cursor.close()
    return jsonify(result)

@app.route('/inventory_items', methods=['POST'])
def add_inventory_item():
    # Get data from the request
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

    # Insert into the database
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

    # Return success response
    return jsonify({"message": "Inventory item added successfully!"}), 201

@app.route('/shopping_items', methods=['GET'])
def get_shopping_items():
    cursor = db.cursor(dictionary=True)
    query = "SELECT * FROM ShoppingItems"
    cursor.execute(query)
    result = cursor.fetchall()
    cursor.close()
    return jsonify(result)

@app.route('/shopping_items', methods=['POST'])
def add_shopping_item():
    # Get data from the request
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

    # Insert into the database
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

    # Return success response
    return jsonify({"message": "Shopping item added successfully!"}), 201


# Run the Flask server
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)


