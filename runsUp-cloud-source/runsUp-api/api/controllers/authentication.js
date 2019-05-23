var passport = require('passport');
var mongoose = require('mongoose');
var Friend = mongoose.model('Friend');
var User = mongoose.model('User');
var requestF = require('request');


// getJsonResponse: take response, status and JSON data and add status and data to response.
var getJsonResponse = function(response, status, data) {
  // Add status and JSON to response.
  response.status(status);
  response.json(data);
};

// authLogIn: log in a user by verifying the username and password
// Return JWT if log in successfull
module.exports.authLogIn = function(request, response) {
	// If username or password missing
	console.log(request.body);
	if (!request.body.accId) {
		getJsonResponse(response, 400, {
			"message": "Missing data"
		});
	}
	
	// Authenticate user and return JWT if authentication successfull
	passport.authenticate('local', function(error, user, data) {
		if (error) {	// If encountered error
			getJsonResponse(response, 404, error);
			return;
		}
		if (user) { 	// If authorization successfull, return generated JWT.
			getJsonResponse(response, 200, {
				"token" : user.generateJwt()
			});
		} else {		// If authorization unsuccessfull...
			getJsonResponse(response, 400, data);
		}
	})(request, response);
};

// authSignUp: create new user and store in DB
module.exports.authSignUp = function(request, response) {
  if(request.body.accId && request.body.profileImageUrl && request.body.name) {
    // Create new user.
    var newUser = new User();
    newUser.setAccId(request.body.accId);
    newUser.workouts = [];
  } else {
    getJsonResponse(response, 400, {
      "message": "Missing data in request body"
    });
    return;
  }
  User.create(newUser, function(error, user) {
    // If there was an error
    if (error) {
      getJsonResponse(response, 500, error);
    } else {
      var newFriend = new Friend();
      newFriend.friendUserId = request.body.accId;
      newFriend.name = request.body.name;
      newFriend.profileImageUrl = request.body.profileImageUrl;
      Friend
        .create(newFriend, function(error, friend) {
          if (error) {
            getJsonResponse(response, 500, error);
          } else {
            getJsonResponse(response, 201, user);
          }
        });
    }
  });
};

module.exports.getAllUsers = function(request, response) {
  User
	.find({}, function(error, results) {
	  if (error) {
		getJsonResponse(response, 500, error);
	  } else if (!results) {
		getJsonResponse(response, 404, {
		  'message' : 'No users found.'
		});
	  } else {
		getJsonResponse(response, 200, results);
	  }
	});
}
