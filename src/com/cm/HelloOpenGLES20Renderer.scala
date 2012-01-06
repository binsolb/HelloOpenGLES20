package com.cm

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock

class HelloOpenGLES20Renderer extends GLSurfaceView.Renderer {

	private var triangleVB : FloatBuffer = null
    private var mProgram : Int =0 
    private var maPositionHandle : Int =0
    private var muMVPMatrixHandle : Int = 0
    private var mMVPMatrix = new Array[Float](16)
    private var mMMatrix = new Array[Float](16)
    private var mVMatrix = new Array[Float](16)
    private var mProjMatrix =new Array[Float](16)
    private var mAngle : Float = 0
  
    def getmAngle() = {
		mAngle
	}

	def setmAngle( mAngle : Float) {
		this.mAngle = mAngle
	}
	
	def incmAngle( dAngle : Float) {
		mAngle += dAngle
	}
	
	val vertexShaderKernel = 
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            """
			uniform mat4 uMVPMatrix;   
            attribute vec4 vPosition;  
            void main(){               
            	// the matrix must be included as a modifier of gl_Position
             	gl_Position = uMVPMatrix * vPosition; 
            
            }"""

    
	val fragmentShaderKernel = """ 
            precision mediump float; 
            void main(){           
            	gl_FragColor = vec4 (0.63671875, 0.76953125, 0.22265625, 1.0); 
            }"""

   
    private def loadShader( shaderType : Int, shaderKernel : String ) : Int = { 
        
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(shaderType) 
        
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderKernel)
        GLES20.glCompileShader(shader);
        
        shader;
    }
	
	private def initShapes() {
        val triangleCoords = new Array[Float](9)
        triangleCoords(0) = -0.5f
        triangleCoords(1) = -0.25f
        triangleCoords(2) = 0
        triangleCoords(3) = 0.5f
        triangleCoords(4) = -0.25f
        triangleCoords(5) = 0
        triangleCoords(6) = 0
        triangleCoords(7) = 0.559016994f
        triangleCoords(8) = 0
          
        // initialize vertex Buffer for triangle  
        val vbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                triangleCoords.length * 4); 
        vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
        triangleVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
        triangleVB.put(triangleCoords);    // add the coordinates to the FloatBuffer
        triangleVB.position(0);            // set the buffer to read the first coordinate
    
    }
	
	override def onSurfaceCreated(unused : GL10, config : EGLConfig ) {
    
        // Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        // initialize the triangle vertex array
        initShapes
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderKernel)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderKernel)
        
        mProgram = GLES20.glCreateProgram             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader)   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(mProgram)                  // creates OpenGL program executables
        
        // get handle to the vertex shader's vPosition member
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")        
   }
   
	override def onDrawFrame(unused : GL10 ) {
    
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT)
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram)
        
        // Prepare the triangle data
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, triangleVB)
        GLES20.glEnableVertexAttribArray(maPositionHandle)
        
        // Apply a ModelView Projection transformation
        Matrix.setRotateM(mMMatrix, 0, mAngle, 0, 0, 1.0f)
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0)
        
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }
    
    override def onSurfaceChanged(unused : GL10 , width : Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        
        val ratio = (1f * width)/ height
        
        // this projection matrix is applied to object coodinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7)
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        
    }

}