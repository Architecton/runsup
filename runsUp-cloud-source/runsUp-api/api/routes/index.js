var express = require('express');
var jwt = require('express-jwt');
var router = express.Router();

var authentication = jwt({
    secret: process.env.JWT_PASSWORD,
    userProperty: 'payload'
});

// controller modules
var ctrlWorkouts = require('../controllers/workouts');
var ctrlFriends = require('../controllers/friends');
var ctrlMessages = require('../controllers/messages')
var ctrlAuthentication = require('../controllers/authentication');

// Controllers for working with workouts
router.get('/workouts/:idUser', authentication, ctrlWorkouts.workoutGetIndices);  				    // TESTED
router.get('/workouts/:idUser/:idWorkout', authentication, ctrlWorkouts.workoutGetById);  		    // TESTED
router.post('/workouts/:idUser', authentication, ctrlWorkouts.workoutAddNew);  					    // TESTED

// Controllers for working with shared workouts.
router.get('/workout_share/:idUser', authentication, ctrlWorkouts.fetchSharedWorkouts);						// TESTED
router.post('/workout_share/:idUser/:idReciever/:idWorkout', authentication, ctrlWorkouts.shareWorkout);    // TESTED

// Controlles for working with friends.
router.get('/friends/:searchTerm', ctrlFriends.searchFriends); 									    // TESTED
router.get('/friends', ctrlFriends.allPotentialFriends);  										    // TESTED
router.post('/friends/:idUser/:idFriend', authentication, ctrlFriends.sendFriendRequest); 		    // TESTED
router.get('/friends/:idUser/fetch', authentication, ctrlFriends.fetchFriends); 		    		// TESTED
router.get('/friends/:idUser/fetch_requests', authentication, ctrlFriends.fetchFriendRequests); 		// TESTED
router.post('/friends/:idUser/accept/:idFriend', authentication, ctrlFriends.acceptFriendRequest);  // TESTED
router.post('/friends/:idUser/reject/:idFriend', authentication, ctrlFriends.rejectFriendRequest);  // TESTED
router.delete('/friends/:idUser/:idFriend', authentication, ctrlFriends.unfriend);

// Controllers for working with messages.
router.get('/messages/:idUser/fetch', authentication, ctrlMessages.fetchMessages); 					// TESTED
router.get('/messages/:idUser/:idSender', authentication, ctrlMessages.getMessagesBySender);		// TESTED 
router.post('/messages/:idUser/:idReceiver', authentication, ctrlMessages.sendMessage);            	// TESTED 
router.delete('/messages/:idUser/:idOther', authentication, ctrlMessages.deleteMessageThread);


// Controllers for authentication
router.post('/users', ctrlAuthentication.authSignUp);  											    // TESTED
router.post('/users/login', ctrlAuthentication.authLogIn);  									    // TESTED

// debug
router.get('/users', ctrlAuthentication.getAllUsers);  											    // TESTED

// Expose router as module.
module.exports = router;
