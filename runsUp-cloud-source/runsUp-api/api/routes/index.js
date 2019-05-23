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
var ctrlAuthentication = require('../controllers/authentication');

// Controllers for working with workouts
router.get('/workouts/:idUser', authentication, ctrlWorkouts.workoutGetIndices);
router.get('/workouts/:idUser/:idWorkout', authentication, ctrlWorkouts.workoutGetById);
router.post('/workouts/:idUser', authentication, ctrlWorkouts.workoutAddNew)

// Controlles for working with friends.
router.get('/friends/:searchTerm', ctrlFriends.searchFriends)
router.get('/friends', ctrlFriends.allPotentialFriends)
router.post('/friends/:idUser/:idFriend', authentication, ctrlFriends.sendFriendRequest)

// Controllers for authentication
router.post('/users', ctrlAuthentication.authSignUp);
router.post('/users/login', ctrlAuthentication.authLogIn);

// debug
router.get('/users', ctrlAuthentication.getAllUsers);

// Expose router as module.
module.exports = router;
