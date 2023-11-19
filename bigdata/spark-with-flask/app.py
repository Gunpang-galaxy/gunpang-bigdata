from flask import Flask,jsonify, make_response, request
from bigdata import getHiveData
import pandas as pd

app = Flask(__name__)

@app.route('/',methods=['GET'], strict_slashes=False)
def getDataUsingSpark():
    playerId = request.args.get('playerId', '')
    date = request.args.get('date','')
    age = request.args.get('age','')
    gender = request.args.get('gender','')
    if playerId=='' or date =='' or age == '' or gender == '':
        return make_response('필수값(playerId, date, age, gender) 없음',400)
    data = getHiveData(playerId, date, age, gender)
    return jsonify(data), 200

if __name__ == '__main__':
    app.run(debug=True, port=8000)