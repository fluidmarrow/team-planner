package com.srivastava.teamplanner.firebase

import android.app.Activity
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.srivastava.teamplanner.activities.*
import com.srivastava.teamplanner.models.Board
import com.srivastava.teamplanner.models.User
import com.srivastava.teamplanner.utils.Constants

class FireStoreClass {

        private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore
            .collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
            activity.userRegisteredSuccess()
        }.addOnFailureListener{
            Log.e(activity.javaClass.simpleName,"Error")
        }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore
            .collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity,"Board Created Successfully",Toast.LENGTH_SHORT).show()
                Log.i(activity.javaClass.simpleName,"Board Created Successfully")
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener{
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating board")
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun loadUserData(activity: Activity){
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get().addOnSuccessListener { document ->
            val loggedInUser = document.toObject(User::class.java)
            when(activity){
                is SignInActivity -> {
                    activity.logInSuccess(loggedInUser)
                }
                is MainActivity -> {
                    activity.updateNavUserDetails(loggedInUser!!,true)

                }
                is MyProfileActivity -> {
                    activity.setUserDataInUI(loggedInUser!!)
                }
            }
        }.addOnFailureListener{
            when(activity){
                is SignInActivity -> {
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error")
                }
                is MainActivity -> {
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error")
                }
            }

        }
    }

    fun getBoardList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
                .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
                .get()
                .addOnSuccessListener {
                    document ->
                    Log.i(activity.javaClass.simpleName,document.documents.toString())
                    val boardList : ArrayList<Board> = ArrayList()
                    for(i in document){
                        val board = i.toObject(Board::class.java)
                        board.documentId = i.id
                        boardList.add(board)
                    }
                    activity.setUpBoardsRecyclerView(boardList)
                }.addOnFailureListener{
                    e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error message",e)
                }
    }

    fun getMemberDetailsList(activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersActivity){
                    activity.setUpMembersList(usersList)
                }
                if(activity is TaskListActivity){
                    activity.boardMembersDetailsList(usersList)
                }
            }.addOnFailureListener{ e ->
                    if(activity is MembersActivity){
                        activity.hideProgressDialog()
                    }
                    if(activity is TaskListActivity){
                        activity.hideProgressDialog()
                    }
                Log.e(activity.javaClass.simpleName,"Error message",e)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap).addOnSuccessListener {
            //Log.i(activity.javaClass.simpleName,"Profile Data Updated Successfully")
            Toast.makeText(activity,"Profile Data Updated Successfully",Toast.LENGTH_SHORT).show()
            activity.profileUpdateSuccess()
        }.addOnFailureListener {
            e -> activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName,"Error while creating a board.",e)
            Toast.makeText(activity,"Profile Data Update Failure",Toast.LENGTH_SHORT).show()
        }
    }

    fun addUpdateTaskList(activity: Activity,board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS).document(board.documentId).update(taskListHashMap).addOnSuccessListener {
            Log.i(activity.javaClass.simpleName,"TaskList Updated Successfully")
            if(activity is TaskListActivity){
                activity.addUpdateTaskListSuccess()
            } else if(activity is CardDetailsActivity){
                activity.addUpdateTaskListSuccess()
            }
        }.addOnFailureListener {
            e ->
            if(activity is TaskListActivity){
                activity.hideProgressDialog()
            } else if(activity is CardDetailsActivity) {
                activity.hideProgressDialog()
            }
            Log.e(activity.javaClass.simpleName,"Error while creating a board.",e)
            Toast.makeText(activity,"TaskList Data Update Failure",Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentUserId(): String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener { document ->
                if(document.size()>0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No Such Member Found")
                }
            }.addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while adding a member.", e)
            }
    }

    fun assignedMemberToBoard(activity: MembersActivity,board: Board,user: User){
        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Members List Updated Successfully")
                activity.membersAssignedSuccess(user)
            }.addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while updating member list.", e)
            }
    }

}