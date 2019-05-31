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
          if ((friend.pendingFriendRequests.filter(x => x.idUser == request.body.idUser)).length > 0) {
            getJsonResponse(response, 400, {
              'message': 'Friend request already sent!'
            });
		  } else if ((friend.friends.filter(x => x.friendUserId == request.body.idUser)).length > 0) {
            getJsonResponse(response, 400, {
              'message': 'Already friends!'
            });
          } else {
            var newPendingFriendRequest = {
              name: request.body.name,
              profileImageUrl: request.body.profileImageUrl,
              idUser: request.body.idUser
            };
            friend.pendingFriendRequests.push(newPendingFriendRequest);
            friend.save(function(error, user) {
              if (error) {
                getJsonResponse(response, 500, error);
              } else {
                getJsonResponse(response, 201, user.pendingFriendRequests.slice(-1)[0]);
              }
            });
          }
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


// acceptFriendRequest: accept friend request from user with specified id.
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


// rejectFriendRequest: reject friend requests from users with specified id.
module.exports.rejectFriendRequest = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params && 
      request.params.idUser && 
      request.params.idFriend && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('pendingFriendRequests')
        .exec(function(error, user) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else if (!user) {
            getJsonResponse(response, 404, {
              'message': 'User not found'
            });
          } else {
            user.pendingFriendRequests = user.pendingFriendRequests.filter(x => x.idUser != request.params.idFriend);
            user.save(function(error, user) {
              if (error) {
                getJsonResponse(response, 500, error);
              } else {
                getJsonResponse(response, 200, {
                  'message': 'Friend request rejected'
                });
              }
            });
          }
        });
    }
  });
}

// fetchFriends: fetch friends of user with specified id.
module.exports.fetchFriends = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('friends')
        .exec(
          function(error, user) {
            if (error) {
              getJsonResponse(response, 400, error);
            } else if(!user.friends) {
              getJsonResponse(response, 404, {
                'message': 'No friends found'
              });
            } else {
              getJsonResponse(response, 200, user.friends);
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

// unfriend: unfriend user with specified id
module.exports.unfriend = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idFriend && request.params.idUser == accId) {
      console.log("HERE");
      User
        .findById(request.params.idUser)
        .select('friends')
        .exec(function(error, user) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else if (!user) {
            getJsonResponse(response, 404, {
              'message': 'No users found'
            });
          } else {
            user.friends = user.friends.filter(x => x.friendUserId != request.params.idFriend);
            user.save(function(error, user) {
              if (error) {
                getJsonResponse(response, 500, error);
              } else {
                User
                  .findById(request.params.idFriend)
                  .select('friends')
                  .exec(function(error, userOther) {
                    if (error) {
                      getJsonResponse(response, 500, error);
                    } else if (!userOther) {
                      getJsonResponse(response, 404, {
                        'message': 'No users found'
                      });
                    } else {
                      userOther.friends = userOther.friends.filter(x => x.friendUserId != request.params.idUser);
                      userOther.save(function(error, user) {
                        if (error) {
                          getJsonResponse(response, 500, error);
                        } else {
                          getJsonResponse(response, 204, {
                            'message': 'Unfriending successful'
                          });
                        }
                      });
                    }
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

// getFriendFullName: get full name of friend with specified id
module.exports.getFriendFullName = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idFriend && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('friends')
        .exec(function(error, user) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else if (!user) {
            getJsonResponse(response, 404, {
              'message': 'User not found.'
            });
          } else if (user && user.friends && user.friends.length > 0) {
			console.log(user);
            const friend = user.friends.filter(x => x.friendUserId == request.params.idFriend)[0];
			console.log(friend);
            getJsonResponse(response, 200, {
              'result': friend.name
            });
          } else {
            getJsonResponse(response, 404, {
              'message': 'specified friend not found'
            });
          }
        });
    } else {
      getJsonResponse(response, 400, {
        'message': 'Bad request parameters'
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
