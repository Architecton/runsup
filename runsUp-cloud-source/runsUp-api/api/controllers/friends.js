var mongoose = require('mongoose');
var Friend = mongoose.model('Friend');
var User = mongoose.model('User');

// REST API database access methods

// getJsonResponse: take response, status and JSON data and add status and data to response.
var getJsonResponse = function(response, status, data) {
  // Add status and JSON to response.
  response.status(status);
  response.json(data);
};

// todoListGetSelected: return todo list with given id of user with given idUser (email)
module.exports.searchFriends = function(request, response) {
  if (request.params && request.params.searchTerm) {
    Friend
      .find({ "name" : { $regex: request.params.searchTerm, $options: 'i' } }, function(error, results) {
          if (!results) {
            getJsonResponse(response, 404, {
              "message": 
                "No users found"
            });
            return;
          } else if (error) {
            getJsonResponse(response, 500, error);
            return;
          } else {
            getJsonResponse(response, 200, results);
          }
        });
  } else {
    getJsonResponse(response, 400, {
      "message": 
        "Invalid request parameters."
    });
  }
};

// todoListGetSelected: return todo list with given id of user with given idUser (email)
module.exports.allPotentialFriends = function(request, response) {
  Friend
    .find({}, function(error, results) {
        if (!results) {
          getJsonResponse(response, 404, {
            "message": 
              "No users found"
          });
          return;
        } else if (error) {
          getJsonResponse(response, 500, error);
        } else {
          getJsonResponse(response, 200, results);
        }
      });
};


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


// addPendingFriendRequestToUser: auxiliary function for sendFriendRequest (see above)
var addPendingFriendRequestToUser = function(request, response, user) {
  if (!user) {
    getJsonResponse(response, 404, {
      "message": "Cannot find user."
    });
  } else {
    User
      .findById(request.params.idFriend)
      .exec(function(error, friend) {
        if (error) {
          getJsonResponse(response, 500, error);
        } else if (!friend) {
          getJsonResponse(response, 404, {
            'message' : 'user with specified id not found.'
          });
        } else {
          var newPendingFriendRequest = {
            name: request.body.name,
            profileImageUrl: request.body.profileImageUrl,
            idUser: request.body.idUser
          };
          user.pendingFriendRequests.push(newPendingFriendRequest);
          user.save(function(error, user) {
            if (error) {
              getJsonResponse(response, 500, error);
            } else {
              getJsonResponse(response, 201, user.pendingFriendRequests.slice(-1)[0]);
            }
          });
        }
      });
  }
};


// fetchFriendRequests: fetch friend requests for users with specified id.
module.exports.fetchFriendRequests = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('pendingFriendRequests')
        .exec(
          function(error, user) {
            if (error) {
              getJsonResponse(response, 400, error);
            } else {
		      getJsonResponse(response, 200, user.pendingFriendRequests);
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


// fetchFriendRequests: fetch friend requests for users with specified id.
module.exports.acceptFriendRequest = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idFriend && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .exec(
          function(error, user) {
            if (error) {
              getJsonResponse(response, 400, error);
            } else {
              Friend
                .find({'friendUserId' : request.params.idFriend}, function(error, results) {
                  if (error) {
                    getJsonResponse(response, 500, error);
                  } else if (!results) {
                    getJsonResponse(response, 404, {
                      'message': 'User not found.'
                    });
                  } else {
                    user.friends.push(results[0]);
                    user.pendingFriendRequests = user.pendingFriendRequests.filter(x => x.idUser != request.params.idFriend);
                    user.save(function(error, user) {
                      if (error) {
                        getJsonResponse(response, 500, error);
                      } else {
                        User
                          .findById(request.params.idFriend, function(error, userOther) {
                            if (error) {
                              getJsonResponse(response, 500, error);
                            } else if (!results) {
                              getJsonResponse(response, 404, {
                                'message' : 'User not found.'
                              });
                            } else {
                              Friend
                                .find({friendUserId: request.params.idUser}, function(error, results) {
                                  if (error) {
                                    getJsonResponse(response, 500, error);
                                  } else if (!results) {
                                    getJsonResponse(response, 404, {
                                      'message' : 'User not found.'
                                    });
                                  } else {
                                    userOther.friends.push(results[0])
                                    userOther.save(function(error, userOther) {
                                      if (error) {
                                        getJsonResponse(response, 500, error);
                                      } else {
                                        getJsonResponse(response, 200, {
                                          'message' : 'Friend request accepted.'
                                        })
                                      }
                                    });
                                  }
                                });
                            }
                          });
                      }
                    });
                  }
                });
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



// sendFriendRequest: send a friend request to a user with specified id.
module.exports.fetchFriends = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idUser == accId) {
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



//////////////////////////////////////////////////////////////////////////

// Get user's id (username) from JWT
var getLoggedId = function(request, response, callback) {
  // If request contains a payload and the payload contains the field "accId"
  if (request.payload && request.payload.accId != undefined) {
    User
      .findById(request.payload.accId)
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
