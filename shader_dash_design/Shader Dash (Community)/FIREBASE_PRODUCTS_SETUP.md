# Setting Up Your Products Database on Firebase

Follow these steps to populate the "Products" section in your app using Firebase Firestore.

## 1. Firebase Configuration

First, you need to connect the app to your Firebase project.

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Select your project.
3. Go to **Project Settings** (the gear icon next to "Project Overview").
4. Scroll down to the **Your apps** section.
5. Under the "SDK setup and configuration" block, copy the `firebaseConfig` object.
6. Open the file `src/app/lib/firebase.ts` in your code editor.
7. Replace the placeholder `firebaseConfig` with your actual configuration.

## 2. Setting Up Firestore Database

1. In the Firebase Console left sidebar, click on **Firestore Database** (under the "Build" menu).
2. Click **Create database** if you haven't already. Choose your preferred location and start in **Test mode** (or configure your security rules as needed).
3. Once the database is created, click **Start collection**.
4. Set the Collection ID to: `products`
5. Click **Next**.

## 3. Adding Products

For each product, you will create a new Document in the `products` collection.

1. Let the **Document ID** Auto-ID (just click "Auto-ID").
2. Add the following Fields to the document (pay attention to the exact spelling and types):

| Field Name  | Type   | Value Example                                                                 |
| :---------- | :----- | :---------------------------------------------------------------------------- |
| `heading`   | string | "Premium Yoga Mat"                                                            |
| `explanation`| string | "High-quality, non-slip yoga mat perfect for home workouts and stretching." |
| `image`     | string | "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=800&q=80"     |
| `link`      | string | "https://amazon.com/example-product-link"                                     |

3. Click **Save**.

### Field Details:
- **`heading`**: The title of the product.
- **`explanation`**: A short description of the product.
- **`image`**: A direct URL to an image. You can host images in Firebase Storage and copy the download URL, or use any direct image link from the web.
- **`link`**: The URL where the user can buy or view the product when they tap the card.

## 4. Viewing the Result

Once you have added documents to the `products` collection and saved your `firebase.ts` file, refresh your app and navigate to the Products tab (the Store icon). You should now see your products beautifully displayed in the Bento grid layout!
