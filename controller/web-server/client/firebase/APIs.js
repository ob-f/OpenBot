import {addDoc, and, collection, doc, getDoc, getDocs, query, where} from 'firebase/firestore'
import {auth, db} from './authentication'
import {localStorageKeys, Month, tables} from '../utils/constants'

/**
 * function to upload user usage on monthly basis on firebase firestore
 * @returns {Promise<void>}
 * @param serverStartTime
 * @param serverEndTime
 */
export async function uploadServerUsage (serverStartTime, serverEndTime) {
    const user = JSON.parse(localStorage.getItem(localStorageKeys.user))
    const details = {
        startTime: serverStartTime,
        uid: user?.uid,
        endTime: serverEndTime
    }
    try {
        await addDoc(collection(db, tables.server),
            details
        ).then(() => {
            console.log('server details successfully added')
        })
    } catch (e) {
        console.log('error::', e)
    }
}

/**
 * function to get document details from the firebase firestore
 * @param value
 * @param table
 * @param fieldName
 * @returns {Promise<null>}
 */
const getDocDetails = async (value, table, fieldName) => {
    try {
        const ordersQuery = query(collection(db, table), and(where(fieldName, '==', value), where('uid', '==', auth?.currentUser.uid)));
        const querySnapshot = await getDocs(ordersQuery)
        let response = null
        querySnapshot.forEach((doc) => {
            return response = {
                data: doc.data(),
                id: doc.id
            }
        })
        return response
    } catch (error) {
        console.log('error :', error)
    }
}

/**
 * function to get user current plan
 * @returns {Promise<undefined|*>}
 */
export async function getUserPlan () {
    const usersRef = doc(db, 'users', auth?.currentUser?.uid)
    try {
        const docSnapshot = await getDoc(usersRef)
        if (!docSnapshot.exists()) {
            return undefined
        }
        const subscriptionData = docSnapshot.data()?.subscription
        if (subscriptionData?.paid) {
            if (new Date() <= subscriptionData.planEndDate) {
                return subscriptionData.planEndDate
            }
        }
    } catch (error) {
        console.error('Error retrieving user plan:', error)
    }
    return undefined
}