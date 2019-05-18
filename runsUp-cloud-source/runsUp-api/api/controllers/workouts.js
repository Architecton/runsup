var mongoose = require('mongoose');
var User = mongoose.model('User');

// REST API database access methods

// getJsonResponse: take response, status and JSON data and add status and data to response.
var getJsonResponse = function(response, status, data) {
  // Add status and JSON to response.
  response.status(status);
  response.json(data);
};


// todoListCreate: add todo list to user with specified username 
module.exports.workoutAddNew = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params.idUser && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('workouts')
        .exec(
          function(error, user) {
            if (error) {
              getJsonResponse(response, 400, error);
            } else {
              addWorkoutToUser(request, response, user);
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
};



// *** AUXILIARY FUNCTION *** //

// addTodoListToUser: auxiliary function for todoListCreate (see above)
var addWorkoutToUser = function(request, response, user) {
  if (!user) {
    getJsonResponse(response, 404, {
      "message": "Cannot find user."
    });
  } else {

    // Create new workout from data
    var newWorkout = request.body;

    // Add todo list to user's list of todo lists.
    user.workouts.push(newWorkout);
    user.save(function(error, user) {
      if (error) {
        getJsonResponse(response, 500, error);
      } else {
        getJsonResponse(response, 201, user.workouts.slice(-1)[0]);
      }
    });
  }
};

// *** /AUXILIARY FUNCTION *** //



module.exports.workoutGetIndices = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params && request.params.idUser && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .exec(function(error, user) {
          if (!user) {  // If user not found
            getJsonResponse(response, 404, {
              "message": 
                "Cannot find user with given identifier idUser."
            });
            return;
          // if error while executing function
          } else if (error) {
            getJsonResponse(response, 500, error);
            return;
          }
          getJsonResponse(response, 200, user.workouts.map(x => x.id));
        });
    } else {
      getJsonResponse(response, 400, { 
        "message": "identifier idUser is missing."
      });
    }
  });
};


// todoListGetSelected: return todo list with given id of user with given idUser (email)
module.exports.workoutGetById = function(request, response) {
  getLoggedId(request, response, function(request, response, accId) {
    if (request.params && request.params.idUser && request.params.idWorkout && request.params.idUser == accId) {
      User
        .findById(request.params.idUser)
        .select('workouts')
        .exec(
          function(error, user) {
            var todoList;
            if (!user) {
              getJsonResponse(response, 404, {
                "message": 
                  "Cannot find user."
              });
              return;
            } else if (error) {
              getJsonResponse(response, 500, error);
              return;
            }
            if (user.workouts && user.workouts.length > 0) {
              workout = user.workouts.id(request.params.idWorkout);
              if (!workout) {
                getJsonResponse(response, 404, {
                  "message": 
                    "cannot find workout"
                });
              } else {
                getJsonResponse(response, 200, workout);
              }
            } else {
              getJsonResponse(response, 404, {
                "message": "cannot find any workouts"
              });
            }
          }
        );
    } else {
      getJsonResponse(response, 400, {
        "message": 
          "Invalid request parameters."
      });
    }
  });
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
            "message": "User not found"
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
      "message": "Inadequate data in token"
    });
    return;
  }
};
