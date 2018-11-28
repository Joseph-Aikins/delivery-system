$(document).ready(() => {
    
    
    firebase.auth().onAuthStateChanged(user => { 
      if (user) {
        console.log("User logged in successfully");
        // Set user email address
        $('#user_profile_name').text(user.email || 'Login to get started');

        // Set user profile image
        $('#user_profile_image').attr('src',user.photoURL || './assets/img/placeholders/default-avatar.png');
        

      } else {
        console.log("No user found");
        window.location = "index.html";
      }
    });

});