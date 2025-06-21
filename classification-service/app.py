import threading

from flask import Flask, request, jsonify
import io
from PIL import Image, ImageOps
from flask_cors import CORS

from Classifier import Classifier
from retrain import retrain_model

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
        result = classifier.detect_and_classify(image)

        if result == "No car detected" or result == "Unknown":
            return jsonify({"contains_car": False, "brand": None, "model": None, "year": None})

        brand, model, year = result
        return jsonify({"contains_car": True, "brand": brand, "model": model, "year": year})

    except Exception as e:
        return jsonify({'error': f"Error during classification: {str(e)}"}), 500

@app.route("/reload-model", methods=["POST"])
def reload_model():
    try:
        data = request.get_json()
        model_path = data.get("path", "model/latest.pt")
        classifier.reload(model_path)
        return jsonify({"status": "success", "message": f"Model reloaded from {model_path}"}), 200
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/retrain-model', methods=['POST'])
def retrain_model_endpoint():
    try:
        threading.Thread(target=retrain_model).start()  # non-blocking
        return jsonify({"status": "started"}), 202
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':

    app.run(debug=True, host='0.0.0.0', port=5000)
