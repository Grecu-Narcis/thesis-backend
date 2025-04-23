from flask import Flask, request, jsonify
import io
from PIL import Image, ImageOps
from flask_cors import CORS

from Classifier import Classifier

app = Flask(__name__)
CORS(app)

classifier = Classifier()

@app.route('/brands', methods=['GET'])
def get_all_brands():
    return jsonify(
        {
            "brands": classifier.get_all_brands()
        }
    )

@app.route('/models/<brand>', methods=['GET'])
def get_all_models(brand):
    return jsonify(
        {
            "models": classifier.get_all_models(brand)
        }
    )

@app.route('/classify', methods=['POST'])
def classify_image():
    image_data = request.data

    if not image_data:
        return jsonify({"error": "No image data provided"}), 400

    image_file = io.BytesIO(image_data)

    # Open the image using PIL
    try:
        image = Image.open(image_file)
        image = ImageOps.exif_transpose(image)
    except Exception as e:
        return jsonify({'error': f"Error opening image: {str(e)}"}), 500

    contains_car = classifier.detect_car(image)
    brand, model, year = classifier.make_prediction(image) if contains_car else (None, None, None)

    return jsonify({"contains_car": contains_car, "brand": brand, "model": model, "year": year})


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
