from flask import Flask,jsonify
from bigdata import getData
app = Flask(__name__)

@app.route('/',methods=['GET'], strict_slashes=False)
def getDataUsingSpark():
    data = getData()
    return jsonify(data)

if __name__ == '__main__':
    app.run(debug=True, port=8000)