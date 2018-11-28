  // Initialize Firebase
var firebase = require("firebase/app");

  var config = {
    apiKey: "AIzaSyAPiO8jKPUCcv-5PjrBhHYGXyUxhtx0Fg4",
    authDomain: "delivery-tracking-system-d346b.firebaseapp.com",
    databaseURL: "https://delivery-tracking-system-d346b.firebaseio.com",
    projectId: "delivery-tracking-system-d346b",
    storageBucket: "delivery-tracking-system-d346b.appspot.com",
    messagingSenderId: "2633226315"
  };

  // Setup configuration
  firebase.initializeApp(config);

  const firestore = firebase.firestore();
  const settings = {/* your settings... */ timestampsInSnapshots: true};
  firestore.settings(settings);
