var mongoose = require('mongoose');
var Friend = mongoose.model('Friend');

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

// addTodoListToUser: auxiliary function for todoListCreate (see above)
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


//////////////////////////////////////////////////////////////////////////

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
