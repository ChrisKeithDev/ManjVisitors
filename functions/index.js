const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.addVisit = functions.https.onCall((data, context) => {
  return admin.firestore().collection("visits").add({
    name: data.name,
    address: data.address,
    purpose: data.purpose,
    dateTime: admin.firestore.FieldValue.serverTimestamp(),
  })
      .then(() => {
        return {result: "Visit added successfully"};
      })
      .catch((error) => {
        throw new functions.https.HttpsError("unknown", "Failed to add", error);
      });
});
