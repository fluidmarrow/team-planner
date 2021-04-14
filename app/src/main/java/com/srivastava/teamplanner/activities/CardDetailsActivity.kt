package com.srivastava.teamplanner.activities

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.srivastava.teamplanner.R
import com.srivastava.teamplanner.dialogs.LabelColorListDialog
import com.srivastava.teamplanner.dialogs.MembersListDialog
import com.srivastava.teamplanner.firebase.FireStoreClass
import com.srivastava.teamplanner.models.Board
import com.srivastava.teamplanner.models.Card
import com.srivastava.teamplanner.models.Task
import com.srivastava.teamplanner.models.User
import com.srivastava.teamplanner.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*


class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListItemPosition: Int = -1
    private var mCardListItemPosition: Int = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailsList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()

        setUpActionBar()

        et_name_card_details.setText(mBoardDetails
            .taskList[mTaskListItemPosition]
            .cardList[mCardListItemPosition]
            .name)

        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListItemPosition].cardList[mCardListItemPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        btn_update_card_details.setOnClickListener{
            if(!et_name_card_details.text.toString().isNullOrEmpty()){
                updateCardDetails()
            } else {
                showErrorSnackBar("Enter a Valid Card Name")
            }
        }

        tv_select_label_color.setOnClickListener {
            labelColorListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL) as Board
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListItemPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListItemPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailsList = intent.getParcelableArrayListExtra<User>(Constants.BOARD_MEMBERS_LIST) as ArrayList<User>
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_card_details_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)

            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

            actionBar.title = mBoardDetails
                .taskList[mTaskListItemPosition]
                .cardList[mCardListItemPosition]
                .name
        }
        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.delete_card -> {
                alertDialogForDeleteList(
                    mBoardDetails
                    .taskList[mTaskListItemPosition]
                    .cardList[mCardListItemPosition]
                    .name
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails(){
        val card = Card(et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListItemPosition].cardList[mCardListItemPosition].createdBy,
            mBoardDetails.taskList[mTaskListItemPosition].cardList[mCardListItemPosition].assignedTo,
            mSelectedColor
            )

        mBoardDetails.taskList[mTaskListItemPosition].cardList[mCardListItemPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun alertDialogForDeleteList(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $cardName.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListItemPosition].cardList
        cardsList.removeAt(mCardListItemPosition)
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListItemPosition].cardList = cardsList
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun colorsList():ArrayList<String>{
        val list: ArrayList<String> = ArrayList()
        list.add("#43C86F")
        list.add("#0C90F1")
        list.add("#F72400")
        list.add("#7A8089")
        list.add("#D57C1D")
        list.add("#770000")
        list.add("#0022F8")
        return list
    }

    private fun setColor(){
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorListDialog(){
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
                this,
                colorsList,
                resources.getString(R.string.str_select_label_color),
                mSelectedColor){
            override fun onItemsSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun membersListDialog(){
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListItemPosition].cardList[mCardListItemPosition].assignedTo
        if(cardAssignedMembersList.size>0){
            for(i in mMembersDetailsList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailsList[i].id == j){
                        mMembersDetailsList[i].selected = true
                    }
                }
            }
        } else {
            for(i in mMembersDetailsList.indices){
                        mMembersDetailsList[i].selected = false
            }
        }
        val listDialog = object : MembersListDialog(
                this,
                mMembersDetailsList,
                resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                TODO("Implement")
            }

        }
    }
}