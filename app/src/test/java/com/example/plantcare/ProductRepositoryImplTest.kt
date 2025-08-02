package com.example.plantcare

import android.content.Context
import android.net.Uri
import com.example.plantcare.model.ProductModel
import com.example.plantcare.repository.ProductRepositoryImpl
import com.google.firebase.database.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ProductRepositoryImplTest {

    @Mock private lateinit var mockDatabase: FirebaseDatabase
    @Mock private lateinit var mockRef: DatabaseReference
    @Mock private lateinit var mockProductRef: DatabaseReference
    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var mockUri: Uri
    @Mock private lateinit var mockSnapshot: DataSnapshot
    @Mock private lateinit var mockError: DatabaseError

    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        `when`(mockDatabase.reference).thenReturn(mockRef)
        `when`(mockRef.child("products")).thenReturn(mockProductRef)

        repository = ProductRepositoryImpl()
        // override default database reference with mocked one
        repository.ref = mockProductRef
    }

    @Test
    fun `addProduct - success`() {
        val product = ProductModel(
            productID = "",
            productName = "Test Plant",
            price = 100.0,
            description = "Test Description",
            image = "https://image.url"
        )
        val callback = mock<(Boolean, String) -> Unit>()

        val pushedKey = "abc123"
        `when`(mockProductRef.push().key).thenReturn(pushedKey)

        val childRef = mock(DatabaseReference::class.java)
        `when`(mockProductRef.child(pushedKey)).thenReturn(childRef)
        `when`(childRef.setValue(product)).thenReturn(mock())

        // Simulate setValue completion listener
        doAnswer {
            val listener = it.getArgument<com.google.android.gms.tasks.OnCompleteListener<Void>>(0)
            listener.onComplete(mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<Void>)
            null
        }.`when`(childRef.setValue(product)).addOnCompleteListener(any())

        repository.addProduct(product, callback)

        verify(callback).invoke(true, "Product added Successfully!")
    }

    @Test
    fun `deleteProduct - success`() {
        val productId = "prod123"
        val callback = mock<(Boolean, String) -> Unit>()

        val childRef = mock(DatabaseReference::class.java)
        `when`(mockProductRef.child(productId)).thenReturn(childRef)
        `when`(childRef.removeValue()).thenReturn(mock())

        doAnswer {
            val listener = it.getArgument<com.google.android.gms.tasks.OnCompleteListener<Void>>(0)
            listener.onComplete(mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<Void>)
            null
        }.`when`(childRef.removeValue()).addOnCompleteListener(any())

        repository.deleteProduct(productId, callback)

        verify(callback).invoke(true, "Product deleted Successfully!")
    }

    @Test
    fun `getProductByID - success`() {
        val productId = "prod123"
        val callback = mock<(ProductModel?, Boolean, String) -> Unit>()

        `when`(mockProductRef.child(productId)).thenReturn(mockProductRef)
        `when`(mockSnapshot.exists()).thenReturn(true)

        val product = ProductModel(
            productID = productId,
            productName = "Test Plant",
            price = 100.0,
            description = "Description",
            image = "https://image.url"
        )
        `when`(mockSnapshot.getValue(ProductModel::class.java)).thenReturn(product)

        doAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onDataChange(mockSnapshot)
            null
        }.`when`(mockProductRef).addValueEventListener(any())

        repository.getProductByID(productId, callback)

        verify(callback).invoke(product, true, "product fetched")
    }

    @Test
    fun `getProductByID - cancelled`() {
        val productId = "prod123"
        val callback = mock<(ProductModel?, Boolean, String) -> Unit>()

        `when`(mockProductRef.child(productId)).thenReturn(mockProductRef)
        `when`(mockError.message).thenReturn("Permission denied")

        doAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onCancelled(mockError)
            null
        }.`when`(mockProductRef).addValueEventListener(any())

        repository.getProductByID(productId, callback)

        verify(callback).invoke(null, false, "Permission denied")
    }

    @Test
    fun `getAllProduct - success`() {
        val callback = mock<(List<ProductModel?>, Boolean, String) -> Unit>()

        `when`(mockSnapshot.exists()).thenReturn(true)

        val child1 = mock(DataSnapshot::class.java)
        val child2 = mock(DataSnapshot::class.java)

        val product1 = ProductModel("id1", "Plant1", 10.0, "Desc1", "img1")
        val product2 = ProductModel("id2", "Plant2", 20.0, "Desc2", "img2")

        `when`(child1.getValue(ProductModel::class.java)).thenReturn(product1)
        `when`(child2.getValue(ProductModel::class.java)).thenReturn(product2)

        `when`(mockSnapshot.children).thenReturn(listOf(child1, child2))

        doAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onDataChange(mockSnapshot)
            null
        }.`when`(mockProductRef).addValueEventListener(any())

        repository.getAllProduct(callback)

        verify(callback).invoke(listOf(product1, product2), true, "product fetched")
    }

    @Test
    fun `getAllProduct - cancelled`() {
        val callback = mock<(List<ProductModel?>, Boolean, String) -> Unit>()

        `when`(mockError.message).thenReturn("Permission denied")

        doAnswer {
            val listener = it.getArgument<ValueEventListener>(0)
            listener.onCancelled(mockError)
            null
        }.`when`(mockProductRef).addValueEventListener(any())

        repository.getAllProduct(callback)

        verify(callback).invoke(emptyList(), false, "Permission denied")
    }
}
