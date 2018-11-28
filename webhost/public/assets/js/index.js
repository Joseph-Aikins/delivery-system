
$(document).ready(() => {
    //Loading bar
    hideSpinner();

    firebase.auth().onAuthStateChanged(user => { 
      if (user) {
        console.log("User logged in successfully.", user.uid);

        //Navigate to the home page
        window.location = "home.html";
      } else {
        console.log("No user found");
      }
    });    

});

  // Create google login
  var googleSignIn = () => {
    // Get auth provider
    var provider = new firebase.auth.GoogleAuthProvider();
    provider.addScope('https://www.googleapis.com/auth/contacts.readonly');
    firebase.auth().useDeviceLanguage();
    provider.setCustomParameters({
      'login_hint': 'user@example.com'
    });

    // Sign in with popup
    firebase.auth().signInWithPopup(provider).then((result)  => {
      // This gives you a Google Access Token. You can use it to access the Google API.
      var token = result.credential.accessToken;
      // The signed-in user info.
      var user = result.user;
      // ...
      console.log(user);
    }).catch((error) => {
      // Handle Errors here.
      var errorCode = error.code;
      var errorMessage = error.message;
      // The email of the user's account used.
      var email = error.email;
      // The firebase.auth.AuthCredential type that was used.
      var credential = error.credential;

      console.log(credential);
      
      
      if (error.code === 'auth/account-exists-with-different-credential') {
        // Step 2.
        // User's email already exists.
        // The pending Google credential.
        var pendingCred = error.credential;
        // The provider account's email address.
        var email = error.email;
        // Get sign-in methods for this email.
        auth.fetchSignInMethodsForEmail(email).then((methods) => {
          // Step 3.
          // If the user has several sign-in methods,
          // the first method in the list will be the "recommended" method to use.
          if (methods[0] === 'password') {
            // Asks the user his password.
            // In real scenario, you should handle this asynchronously.
            var password = promptUserForPassword(); // TODO: implement promptUserForPassword.
            auth.signInWithEmailAndPassword(email, password).then(function(user) {
              // Step 4a.
              return user.link(pendingCred);
            }).then(function() {
              // Google account successfully linked to the existing Firebase user.
              goToApp();
            });
            return;
          }

          // All the other cases are external providers.
          // Construct provider object for that provider.
          // TODO: implement getProviderForProviderId.
          var provider = getProviderForProviderId(methods[0]);
          // At this point, you should let the user know that he already has an account
          // but with a different provider, and let him validate the fact he wants to
          // sign in with this provider.
          // Sign in to provider. Note: browsers usually block popup triggered asynchronously,
          // so in real scenario you should ask the user to click on a "continue" button
          // that will trigger the signInWithPopup.
          auth.signInWithPopup(provider).then(function(result) {
            // Remember that the user may have signed in with an account that has a different email
            // address than the first one. This can happen as Firebase doesn't control the provider's
            // sign in flow and the user is free to login using whichever account he owns.
            // Step 4b.
            // Link to Google credential.
            // As we have access to the pending credential, we can directly call the link method.
            result.user.linkAndRetrieveDataWithCredential(pendingCred).then(function(usercred) {
              // Google account successfully linked to the existing Firebase user.
              goToApp();
            });
          });
        });

    }
  });
};

// Navigate user to the application
var goToApp = () => {
  console.log("User logged in successfully.");

  //Navigate to the home page
  window.location = "home.html";
};

// logout user
var logout = () => {
    firebase.auth().signOut().then(function() {
      // Sign-out successful.
    }).catch(function(error) {
      // An error happened.
      hideSpinner();
    });
};

// Login
var login = () => {
  // Get fields
  const email = $('#emailField').val();
  const password = $('#passwordField').val();

  if (email === '') {
    alert("Please enter a valid email address");
    return;
  } else if (password === '') {
    alert("Please enter a strong password");
    return;
  } else if (email != 'admin@delivery.com') {
    alert("This account is not registered as an administrator");
  } else {
    showSpinner();
    firebase.auth().signInWithEmailAndPassword(email,password).then((result) => {
      console.log("Logged in successfully", result.user.email);
    }).catch((err) => {
      hideSpinner();
      console.log(err.message);
      alert(err.message);
    });
  }
};

// Forget password
var fgtPwd = () => {
  
};

// Register
var register = () => {
  window.location = 'register.html';
};

var hideSpinner = () => {
  $('#overlay').hide();
};

var showSpinner = () => {
  $('#overlay').show();
};
