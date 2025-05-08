from flask import Flask, request, jsonify
import io
from PIL import Image, ImageOps
from flask_cors import CORS

from Classifier import Classifier

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024
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

    try:
        image_file = io.BytesIO(image_data)
        image = Image.open(image_file)
        image = ImageOps.exif_transpose(image)
    except Exception as e:
        print('first exception', e)
        return jsonify({'error': f"Error opening image: {str(e)}"}), 500

    try:
        contains_car = classifier.detect_car(image)
        if contains_car:
            brand, model, year = classifier.make_prediction(image)
        else:
            brand, model, year = (None, None, None)
    except Exception as e:
        return jsonify({'error': f"Error during classification: {str(e)}"}), 500

    return jsonify({"contains_car": contains_car, "brand": brand, "model": model, "year": year})


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
