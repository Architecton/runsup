var mongoose = require('mongoose');

var messageSchema = new mongoose.Schema({
  fromId: {type: Number, required: true},
  toId: {type: Number, required: true},
  content: {type: String}
})

mongoose.model('Message', messageSchema, 'Messages');
module.exports = messageSchema;

