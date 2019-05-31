var mongoose = require('mongoose');
var User = mongoose.model('User');
var Message = mongoose.model('Message');

// getJsonResponse: take response, status and JSON data and add status and data to response.
var getJsonResponse = function(response, status, data) {

  // Add status and JSON to response.
  response.status(status);
  response.json(data);
};


module.exports.fetchMessages = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('messages')
        .exec(function(error, results) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else if (!results) {
            getJsonResponse(response, 404, {
              'message' : 'User not found.'
            });
          } else {
            if (results.messages) {
              getJsonResponse(response, 200, results.messages);
            } else {
              getJsonResponse(response, 404, );
            }
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message': 'Bad request parameters.'
      });
    }
  });
} 


module.exports.getMessagesBySender = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idSender && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('messages')
        .exec(function(error, user) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else if (!user) {
            getJsonResponse(response, 404, {
              'message' : 'User not found.'
            });
          } else {
            getJsonResponse(response, 200, user.messages.filter(x => x.idSender == request.params.idSender || x.idSender == request.params.idUser));
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message': 'Bad request parameters.'
      });
    }
  });
}


module.exports.sendMessage = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser 
      && request.params.idReceiver 
      && request.body.content
	  && request.body.profileImageUri	
	  && request.body.senderName
      && request.params.idUser == accId) {
      User
        .findById(request.params.idReceiver)
        .select('messages')
        .exec(function(error, user) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else if (!user) {
            getJsonResponse(response, 404, {
              'message': 'User not found.'
            });
          } else {
            var newMessage = new Message();
            newMessage.idReceiver = request.params.idReceiver;
            newMessage.idSender = request.params.idUser;
            newMessage.content = request.body.content;
            newMessage.sentDate = Date.now();
            newMessage.profileImageUri = request.body.profileImageUri;
            newMessage.senderName = request.body.senderName
            user.messages.push(newMessage);
            user.save(function(error, user) {
              if (error) {
                getJsonResponse(response, 500, error);
              } else {
                User
                  .findById(request.params.idUser)
                  .select('messages')
                  .exec(function(error, userOther) {
                    if (error) {
                      getJsonResponse(response, 500, error);
                    } else if (!userOther) {
                      getJsonResponse(response, 404, {
                        'message' : 'User not found.'
                      });
                    } else {
                      userOther.messages.push(newMessage);
                      userOther.save(function(error, userOther) {
                        if (error) {
                          getJsonResponse(response, 500, error);
                        } else {
                          getJsonResponse(response, 201, {
                            'message' : 'Message sent.'
                          });
                        }
                      })
                    }
                  });
              }
            });
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message' : 'Bad request parameters.'
      });
    } 
  });
}

// deleteMessageThread: delete thread of messages with specified friend.
module.exports.deleteMessageThread = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params && request.params.idUser && request.params.idOther && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('messages')
        .exec(function(error, user) {
          if (!user) {
            getJsonResponse(response, 404, {
              'message': 'User not found'
            });
          } else {
            // Filter out messages sent by specified friend.
            user.messages = user.messages.filter(x => x.idSender != request.params.idOther && x.idReceiver != request.params.idOther);
            user.save(function(error, user) {
              if (error) {
                getJsonResponse(response, 500, error);
              } else {
                getJsonResponse(response, 204, {
                  'message': 'Messages deleted successfully.'
                });
              }
            });
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message': 'Bad request parameters.'
      });
    }
  });
}

// Get user's id (username) from JWT
var getLoggedId = function(request, response, callback) {
  // If request contains a payload and the payload contains the field "accId"
  if (request.payload && request.payload.accId != undefined) {
    User
      .findById(
        request.payload.accId
      )
      .exec(function(error, user) {
        if (!user) {     // If user not found
          getJsonResponse(response, 404, {
            "message": "User not found."
          });
          return;
        } else if (error) {   // if encountered error
          getJsonResponse(response, 500, error);
          return;
        }
        callback(request, response, user._id);
      });
  } else {    // Else if no payload or if payload does not contain field "id"
    getJsonResponse(response, 400, {
      "message": "Inadequate data in token."
    });
    return;
  }
};

