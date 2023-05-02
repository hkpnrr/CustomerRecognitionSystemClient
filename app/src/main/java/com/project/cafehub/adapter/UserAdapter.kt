package com.project.cafehub.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import com.project.cafehub.databinding.RowUserBinding
import com.project.cafehub.model.CurrentUser
import com.project.cafehub.model.User
import com.squareup.picasso.Picasso
import java.util.Calendar
import java.util.Date

class UserAdapter(val userList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserHolder>() {

    private lateinit var db: FirebaseFirestore
    class UserHolder(val binding: RowUserBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val binding = RowUserBinding.inflate(LayoutInflater.from(parent.context))
        db = Firebase.firestore
        return UserHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val namesurname = "${userList[position].name} ${userList[position].surname}"
        holder.binding.tvUserName.text = namesurname
        Picasso.get().load(userList[position].photoUrl).into(holder.binding.ivUserPhoto)
        holder.binding.ivAddFriend.setOnClickListener {
            if(CurrentUser.user.id==userList[position].id){
                println("Kendine istek atamazsın.")
            }
            else{

                //check if this user already friends or requested

                db.collection("User").document(CurrentUser.user.id.toString())
                    .collection("Friends").whereEqualTo("friendId",userList[position].id)
                    .get().addOnSuccessListener { it->
                        if(it.isEmpty){
                            // not friend
                            println("arkadaş değilsin")
                            db.collection("FriendshipRequest").whereEqualTo("requesterId",CurrentUser.user.id.toString())
                                .whereEqualTo("isValid",true)
                                .get().addOnSuccessListener {
                                    if (it.isEmpty){
                                        //never sent request
                                        println("arkadaş isteği yollamamışsın")
                                        val request = hashMapOf(
                                            "requesterId" to CurrentUser.user.id.toString(),
                                            "addresseeId" to userList[position].id.toString(),
                                            "requesterName" to CurrentUser.user.name.toString(),
                                            "requesterSurname" to CurrentUser.user.surname.toString(),
                                            "addresseeName" to userList[position].name.toString(),
                                            "addresseeSurname" to userList[position].surname.toString(),
                                            "sendTime" to Calendar.getInstance().toString(),
                                            "requesterPhotoUrl" to CurrentUser.user.photoUrl.toString(),
                                            "addresseePhotoUrl" to userList[position].photoUrl.toString(),
                                            "isValid" to true,
                                            "isAccepted" to false
                                        )
                                        db.collection("FriendshipRequest").add(request).addOnSuccessListener {
                                            //request sent successfull
                                            println("arkadaş isteği yollandı")
                                        }.addOnFailureListener {

                                        }
                                    }
                                    else{
                                        //request already sent
                                        println("arkadaş isteği zaten yollamışsın")
                                    }
                                }.addOnFailureListener {

                                }

                        }
                        else{
                            //already friends
                            println("arkadaşsın zaten")
                        }
                    }.addOnFailureListener {

                    }


            }
        }
    }
}