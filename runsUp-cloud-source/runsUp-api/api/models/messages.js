var mongoose = require('mongoose');

var messageSchema = new mongoose.Schema({
  idReceiver: {type: Number, required: true},
  idSender: {type: Number, required: true},
  content: {type: String},
  sentDate: {type: Date},
  profileImageUri: {type: String},
  senderName: {type: String}
})

mongoose.model('Message', messageSchema, 'Messages');
module.exports = messageSchema;

