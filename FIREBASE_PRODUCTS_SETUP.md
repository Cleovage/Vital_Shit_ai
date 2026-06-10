# Setting Up Your Products Database on Firebase for Android

Follow these steps to populate the "Products" section in your Kotlin Android app using Firebase Firestore.

## 1. Setting Up Firestore Database

Since your Android app is already connected to Firebase (via `google-services.json`), you just need to set up the Firestore database.

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Select your Android project.
3. In the left sidebar, click on **Firestore Database** (under the "Build" menu).
4. Click **Create database** if you haven't already. Choose your preferred location and start in **Test mode** (or configure your security rules to allow reads).
5. Once the database is created, click **Start collection**.
6. Set the Collection ID exactly to: `products`
7. Click **Next**.

## 2. Adding Products

For each product you want to display in the Bento grid, you will create a new Document in the `products` collection.

1. Let the **Document ID** be Auto-ID (just click "Auto-ID").
2. Add the following Fields to the document. **Pay attention to the exact spelling (all lowercase) and types**:

| Field Name    | Type   | Value Example                                                                 |
| :------------ | :----- | :---------------------------------------------------------------------------- |
| `heading`     | string | "Premium Yoga Mat"                                                            |
| `explanation` | string | "High-quality, non-slip yoga mat perfect for home workouts and stretching."   |
| `image`       | string | "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=800&q=80"     |
| `link`        | string | "https://amazon.com/example-product-link"                                     |

3. Click **Save**.

### Field Details:
- **`heading`**: The bold title of the product.
- **`explanation`**: A short description of the product.
- **`image`**: A direct URL to an image. You can use images hosted anywhere on the internet (like Unsplash) or host them in Firebase Storage and copy the download URL. The app uses Coil to load these images efficiently.
- **`link`**: The URL that will open in the browser when the user taps on the product card.

## 3. Viewing the Result

Once you have added documents to the `products` collection in your Firebase Console, simply run the app on your Android device or emulator. Navigate to the new "Products" tab (the Store icon) in the bottom navigation bar, and you should see your products dynamically loaded into the beautiful staggered Bento grid layout!
