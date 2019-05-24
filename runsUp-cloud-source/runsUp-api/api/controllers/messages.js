
module.exports.fetchMessages = function() {
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
            getJsonResponse(response, 200, results);
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message': 'Bad request parameters.'
      });
    }
  });
} 

module.exports.getMessagesBySender = function() {
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
            getJsonResponse(response, 200, user.messages.filter(x => x.idSender == request.params.idSender));
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message': 'Bad request parameters.'
      });
    }
  });
}

module.exports.sendMessage = function() {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser 
      && request.params.idReciever 
      && request.body.content
      && request.params.idUser == accId) {
      User
        .findById(request.params.idReciever)
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
            newMessage.fromId = request.params.idUser;
            newMessage.toId = request.params.idReciever;
            newMessage.content = request.body.content;
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

// sendFriendRequest: send a friend request to a user with specified id.
module.exports.sendFriendRequest = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idFriend && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('pendingFriendRequests')
        .exec(
          function(error, user) {
            if (error) {
              getJsonResponse(response, 400, error);
            } else {
              addPendingFriendRequestToUser(request, response, user);
            }
          }
        );
    } else {
      getJsonResponse(response, 400, {
        "message":
          "Bad request parameters"
      });
    }
  });
}
