var mongoose = require("mongoose");
var crypto = require('crypto');
var jwt = require('jsonwebtoken');
var friendSchema = require('./friends');
var messageSchema = require('./messages');


// Schema representing a gps point of a workout
var gpsPointSchema = new mongoose.Schema({
  _id: {type: Number},
  sessionNumber: {type: Number},
  latitude: {type: Number},
  longitude: {type: Number},
  elevation: {type: Number},
  duration: {type: Number},
  speed: {type: Number},
  pace: {type: Number},
  totalCalories: {type: Number},
  created: {type: String},
  lastUpdate: {type: String},
  pauseFlag: {type: Number}
});


// Schema representing a workout
var workoutSchema = new mongoose.Schema({
  _id: {type : Number, required: true, unique: true},
  gpsPoints: [gpsPointSchema],
  title: {type : String},
  created: {type: String},
  status: {type: Number},
  distance: {type: Number},
  duration : {type : Number},
  totalCalories: {type: Number},
  paceAvg: {type: Number},
  sportActivity: {type: Number},
  lastUpdate: {type: String}
});


var pendingFriendRequestSchema = new mongoose.Schema({
  name: {type: String, required: true},
  profileImageUrl: {type: String, required: true},
  idUser: {type: Number, required: true}
});


// Schema representing a user
var userSchema = new mongoose.Schema({                      
  _id: {type: Number, required: true, unique: true},
  workouts: [workoutSchema],
  pendingFriendRequests: [pendingFriendRequestSchema],
  friends: [friendSchema],
  messages: [messageSchema],
  sharedWorkouts: [workoutSchema]
});


// setAccId: Set user's account id
userSchema.methods.setAccId = function(accId) {
  this._id = accId;
};


// checkAccId: Check account id
userSchema.methods.checkAccId = function(accId) {
  return this._id == accId;
};


// generateJwt: generate Json Web Token
userSchema.methods.generateJwt = function() {
  var expirationDate = new Date();
  expirationDate.setDate(expirationDate.getDate() + 7);
  return jwt.sign({
    accId: this._id,
    expirationDate: parseInt(expirationDate.getTime() / 1000, 10)
  }, process.env.JWT_PASSWORD);
};


// Compile the schema into a model.
// Name of model, schema to be used, optional name of the mongoDB collection
mongoose.model('User', userSchema, 'Users');
