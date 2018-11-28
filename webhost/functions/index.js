const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
admin.initializeApp();

// Welcome new users
exports.createUser = functions.firestore
  .document('users/{userId}')
  .onCreate((snap, context) => {
    // Get an object representing the document
    // e.g. {'name': 'Marie', 'age': 66}
    const newValue = snap.data();

    // access a particular field as you would any JS property
    const name = newValue.name;

    // perform desired operations ...

    return console.log(newValue);
  });

exports.requestRider = functions.firestore
  .document('test_orders/{itemId}')
  .onCreate((snap, context) => {
    //Read data from the database
    const order = snap.data();

    return admin.firestore().collection('tokens').get()
      .then((token_result) => {
        return token_result.forEach(token => {
          const message = {
            data: {
              order: `${order.key}`
            },
            token: token.data().token
          }

          console.log(token);

          return admin.messaging().send(message, true).then((result) => {
            return console.log("Message sent to riders and users");
          }).catch((err) => {
            if (err) {
              return console.log("Message could not be sent. ", err.message);
            }
          });
        });
      }).catch((token_err) => {
        if (token_err) {
          return console.log("Tokens could not be retrieved. ", token_err.message);
        }
      });
  });

exports.requestUserAuth = functions.firestore
  .document('orders_authentication/{customer_uid}')
  .onCreate((snap, context) => {
    //Read data from the database
    const data = snap.data();

    // Message body
    const message = {
      data: {
        message: `Hello, ${data.customer}! Please authorize this transaction from ${data.rider} for the purchase of some items.`,
        riderId: `${data.riderId}`,
        customerId: `${data.customerId}`
      },
      token: data.token
    }

    if (data.email === null || data.email === '') {
      return admin.messaging().send(message, true).then((result) => {
        return console.log("Message sent to customer for authentication. Body: ", message.data.message);
      }).catch((err) => {
        if (err) {
          return console.log("Message could not be sent. ", err.message);
        }
      });
    } else {
      return sendEmail(data.email, Math.floor(Math.random() * 999999) + 111111);
    }
  });

// Receive new requests
exports.createRequest = functions.firestore
  .document('requests/{itemId}')
  .onCreate((snap, context) => {
    // Get an object representing the document
    // e.g. {'name': 'Marie', 'age': 66}
    const newValue = snap.data();

    // access a particular field as you would any JS property
    const name = newValue.name;

    // perform desired operations ...

    return console.log(newValue);

  });

// Send Email to customer with short code if there is no phone number attached
var sendEmail = (address, code) => {
  // Generate test SMTP service account from ethereal.email
  // Only needed if you don't have a real mail account for testing
  console.log(`Address: ${address} with code: ${code}`);

  nodemailer.createTestAccount((err, account) => {
    if (err) {
      return console.log("An error occurred while sending the email to " + address, err);
    }
    // create reusable transporter object using the default SMTP transport
    let transporter = nodemailer.createTransport({
      host: 'smtp.ethereal.email',
      port: 587,
      secure: false, // true for 465, false for other ports
      auth: {
        user: account.user, // generated ethereal user
        pass: account.pass // generated ethereal password
      }
    });

    // setup email data with unicode symbols
    let mailOptions = {
      from: '"Awesome Delivery Systems" <codelbas.quabynah@gmail.com>', // sender address
      to: address || 'quabynahdennis@gmail.com',
      subject: 'Short code for order authentication', // Subject line
      text: `Hello cherished customer! Your short code is: ${code}`, // plain text body
      html: `<b>Hello cherished customer! Your short code is </b>` // html body
    };

    // send mail with defined transport object
    transporter.sendMail(mailOptions, (error, info) => {
      if (error) {
        return console.log(error);
      }
      console.log('Message sent: %s. Address to: %s', info.messageId, mailOptions.to);
      // Preview only available when sending through an Ethereal account
      console.log('Preview URL: %s', nodemailer.getTestMessageUrl(info));

      // Message sent: <b658f8ca-6296-ccf4-8306-87d57a0b4321@example.com>
      // Preview URL: https://ethereal.email/message/WaQKMgKddxQDoou...
    });
  });
};