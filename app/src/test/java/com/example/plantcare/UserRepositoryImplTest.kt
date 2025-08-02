package com.example.plantcare

import com.example.plantcare.model.UserModel
import com.example.plantcare.repository.UserRepositoryImpl
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserRepositoryImplTest {

    @Mock private lateinit var mockAuth: FirebaseAuth
    @Mock private lateinit var mockUser: FirebaseUser
    @Mock private lateinit var mockDatabase: FirebaseDatabase
    @Mock private lateinit var mockRef: DatabaseReference
    @Mock private lateinit var mockUserRef: DatabaseReference
    @Mock private lateinit var mockAuthTask: Task<AuthResult>
    @Mock private lateinit var mockVoidTask: Task<Void>

    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setUp() {
        `when`(mockDatabase.reference).thenReturn(mockRef)
        `when`(mockRef.child("users")).thenReturn(mockUserRef)

        userRepository = UserRepositoryImpl(
            auth = mockAuth,
            database = mockDatabase
        )
    }

    @Test
    fun `login - success`() {
        val email = "test@gmail.com"
        val password = "123456"
        val callback = mock<(Boolean, String) -> Unit>()

        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(true)

        doAnswer {
            val listener = it.getArgument<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            null
        }.`when`(mockAuthTask).addOnCompleteListener(any())

        userRepository.login(email, password, callback)

        verify(callback).invoke(true, "Login Successfully")
    }

    @Test
    fun `register - failure`() {
        val email = "fail@gmail.com"
        val password = "123"
        val callback = mock<(Boolean, String, String) -> Unit>()

        `when`(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(Exception("Registration failed"))

        doAnswer {
            val listener = it.getArgument<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            null
        }.`when`(mockAuthTask).addOnCompleteListener(any())

        userRepository.register(email, password, callback)

        verify(callback).invoke(false, "Registration failed", "")
    }

    @Test
    fun `forgetPassword - success`() {
        val email = "test@gmail.com"
        val callback = mock<(Boolean, String) -> Unit>()

        `when`(mockAuth.sendPasswordResetEmail(email)).thenReturn(mockVoidTask)
        `when`(mockVoidTask.isSuccessful).thenReturn(true)

        doAnswer {
            val listener = it.getArgument<com.google.android.gms.tasks.OnCompleteListener<Void>>(0)
            listener.onComplete(mockVoidTask)
            null
        }.`when`(mockVoidTask).addOnCompleteListener(any())

        userRepository.forgetPassword(email, callback)

        verify(callback).invoke(true, "Reset email sent to $email")
    }

    @Test
    fun `addUserToDatabase - success`() {
        val userId = "123"
        val model = UserModel("Test", "test@gmail.com", "123456", "123")
        val callback = mock<(Boolean, String) -> Unit>()

        `when`(mockUserRef.child(userId)).thenReturn(mockUserRef)
        `when`(mockUserRef.setValue(model)).thenReturn(mockVoidTask)
        `when`(mockVoidTask.isSuccessful).thenReturn(true)

        doAnswer {
            val listener = it.getArgument<com.google.android.gms.tasks.OnCompleteListener<Void>>(0)
            listener.onComplete(mockVoidTask)
            null
        }.`when`(mockVoidTask).addOnCompleteListener(any())

        userRepository.addUserToDatabase(userId, model, callback)

        verify(callback).invoke(true, "Registration Succesfull")
    }

    @Test
    fun `logout - success`() {
        val callback = mock<(Boolean, String) -> Unit>()
        doNothing().`when`(mockAuth).signOut()

        userRepository.logout(callback)

        verify(callback).invoke(true, "Logout successful")
    }

    @Test
    fun `getUserByID - success`() {
        val userId = "user123"
        val callback = mock<(UserModel?, Boolean, String) -> Unit>()
        val snapshot = mock(DataSnapshot::class.java)
        val userModel = UserModel("Test", "test@gmail.com", "123456", "123")

        `when`(mockUserRef.child(userId)).thenReturn(mockUserRef)
        `when`(snapshot.exists()).thenReturn(true)
        `when`(snapshot.getValue(UserModel::class.java)).thenReturn(userModel)

        doAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onDataChange(snapshot)
            null
        }.`when`(mockUserRef).addValueEventListener(any())

        userRepository.getUserByID(userId, callback)

        verify(callback).invoke(userModel, true, "data fetched")
    }

    @Test
    fun `getUserByID - cancelled`() {
        val userId = "user123"
        val callback = mock<(UserModel?, Boolean, String) -> Unit>()
        val error = mock(DatabaseError::class.java)

        `when`(mockUserRef.child(userId)).thenReturn(mockUserRef)
        `when`(error.message).thenReturn("Permission denied")

        doAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onCancelled(error)
            null
        }.`when`(mockUserRef).addValueEventListener(any())

        userRepository.getUserByID(userId, callback)

        verify(callback).invoke(null, false, "Permission denied")
    }
}
