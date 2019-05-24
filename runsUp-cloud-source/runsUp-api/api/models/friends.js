var mongoose = require("mongoose");

// Schema representing a user
var friendSchema = new mongoose.Schema({                      
  friendUserId: {type: Number, required: true},
  name: {type: String, required: true},
  profileImageUrl: {type: String},
  friendsSince: {type: String, default: Date.now},
  dateJoined: {type: String, default: Date.now}
});


// Compile the schema into a model.
// Name of model, schema to be used, optional name of the mongoDB collection
mongoose.model('Friend', friendSchema, 'Friends');
module.exports = friendSchema;
