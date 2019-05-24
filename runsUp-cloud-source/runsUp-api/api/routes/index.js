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

// Controlles for working with friends.
router.get('/friends/:searchTerm', ctrlFriends.searchFriends); 									    // TESTED
router.get('/friends', ctrlFriends.allPotentialFriends);  										    // TESTED
router.post('/friends/:idUser/:idFriend', authentication, ctrlFriends.sendFriendRequest); 		    // TESTED
router.get('/friends/:idUser/fetch', authentication, ctrlFriends.fetchFriendRequests); 			    // TESTED
router.post('/friends/:idUser/accept/:idFriend', authentication, ctrlFriends.acceptFriendRequest);  // TESTED

// Controllers for working with messages.
router.get('/messages/:idUser/fetch', authentication, ctrlMessages.fetchMessages);
router.get('/messages/:idUser/:idSender', authentication, ctrlMessages.getMessagesBySender);
router.post('/messages/:idUser/:idReciever', authentication, ctrlMessages.sendMessage);

// Controllers for working with shared workouts.
router.get('workouts/share/:idUser', authentication, ctrlWorkouts.fetchSharedWorkouts);
router.post('workouts/share/:idUser/:idReciever/:idWorkout', authentication, ctrlWorkouts.shareWorkout);

// Controllers for authentication
router.post('/users', ctrlAuthentication.authSignUp);  											    // TESTED
router.post('/users/login', ctrlAuthentication.authLogIn);  									    // TESTED

// debug
router.get('/users', ctrlAuthentication.getAllUsers);  											    // TESTED

// Expose router as module.
module.exports = router;
