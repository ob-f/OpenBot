import {initializeApp} from 'firebase/app'
import {getAuth, signOut, signInWithPopup, GoogleAuthProvider} from 'firebase/auth'
import {getFirestore} from 'firebase/firestore'
import {getStorage} from 'firebase/storage'
import {localStorageKeys} from '../utils/constants'

// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: import.meta.env.VITE_PUBLIC_FIREBASE_API_KEY,
    authDomain: import.meta.env.VITE_PUBLIC_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_PUBLIC_PROJECT_ID,
    storageBucket: import.meta.env.VITE_PUBLIC_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_PUBLIC_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_PUBLIC_APP_ID,
    measurementId: import.meta.env.VITE_PUBLIC_MEASUREMENT_ID,
}

const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
const provider = new GoogleAuthProvider()
export const FirebaseStorage = getStorage(app)
export const db = getFirestore()

/**
 * Function to google Sign in
 * @returns {Promise<unknown>}
 */
export function googleSigIn () {
    return new Promise((resolve, reject) => {
        signInWithPopup(auth, provider)
            .then((result) => {
                // The signed-in user info.
                const user = result.user
                resolve(user)
            })
            .catch((error) => {
                // Handle Errors here.
                reject(error)
            })
    })
}

/**
 * function to log out user from Google account
 * @returns {Promise<void>}
 */
export function googleSignOut () {
    signOut(auth).then(() => {
        localStorage.setItem(localStorageKeys.isSignIn, 'false')
    }).catch((error) => {
        console.log('Sign-out error ', error)
    })
}
