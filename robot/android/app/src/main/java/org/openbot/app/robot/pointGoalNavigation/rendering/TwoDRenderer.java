package org.openbot.app.robot.pointGoalNavigation.rendering;

/*
Copyright 2017 Google Inc.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * This class is used to render 2D images using OpenGL From:
 * https://gist.github.com/claywilkinson/b1d9c8cbfd6bb48253ead802034e304b#file-quadrenderer-java
 */
public class TwoDRenderer {
  private static final String TAG = TwoDRenderer.class.getName();

  private int[] mTextures = new int[1];

  private static final float[] QUAD_COORDS =
      new float[] {
        // x, y, z
        -.05f, -.1f, 0.0f, -.05f, +.05f, 0.0f, +.05f, -.1f, 0.0f, +.05f, +.05f, 0.0f,
      };

  private static final float[] QUAD_TEXCOORDS =
      new float[] {
        // x, y
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
      };

  private static final int COORDS_PER_VERTEX = 3;
  private static final int TEXCOORDS_PER_VERTEX = 2;

  // Shader source code, since they are small, just include them inline.
  private static final String VERTEX_SHADER =
      "uniform mat4 u_ModelViewProjection;\n"
          + "attribute vec4 a_Position;\n"
          + "attribute vec2 a_TexCoord;\n"
          + "\n"
          + "varying vec2 v_TexCoord;\n"
          + "\n"
          + "void main() {\n"
          + "gl_Position = u_ModelViewProjection * a_Position;\n"
          + "   v_TexCoord = a_TexCoord;\n"
          + "}";

  private static final String FRAGMENT_SHADER =
      "precision mediump float;\n"
          + "uniform sampler2D u_Texture;\n"
          + "varying vec2 v_TexCoord;\n"
          + "\n"
          + "void main() {\n"
          + "   gl_FragColor = texture2D(u_Texture, v_TexCoord);\n"
          + "}\n";

  private FloatBuffer mQuadVertices;
  private FloatBuffer mQuadTexCoord;
  private int mQuadProgram;
  private int mQuadPositionParam;
  private int mQuadTexCoordParam;
  private int mTextureUniform;
  private int mModelViewProjectionUniform;

  // Temporary matrices allocated here to reduce number of allocations for each frame.
  private float[] mModelMatrix = new float[16];
  private float[] mModelViewMatrix = new float[16];
  private float[] mModelViewProjectionMatrix = new float[16];

  public void createOnGlThread(Context context, String pngName) {
    // Read the texture.
    Bitmap textureBitmap = null;
    try {
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inPreferredConfig = Config.ARGB_8888;
      textureBitmap = BitmapFactory.decodeStream(context.getAssets().open(pngName), null, opts);
    } catch (IOException e) {
      Log.e(TAG, "Exception reading texture", e);
      return;
    }

    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    GLES20.glEnable(GLES20.GL_BLEND);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glGenTextures(mTextures.length, mTextures, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    textureBitmap.recycle();

    ShaderUtil.checkGLError(TAG, "Texture loading");

    // Build the geometry of a simple quad.

    int numVertices = 4;
    if (numVertices != QUAD_COORDS.length / COORDS_PER_VERTEX) {
      throw new RuntimeException("Unexpected number of vertices in BackgroundRenderer.");
    }

    ByteBuffer bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.length * Float.BYTES);
    bbVertices.order(ByteOrder.nativeOrder());
    mQuadVertices = bbVertices.asFloatBuffer();
    mQuadVertices.put(QUAD_COORDS);
    mQuadVertices.position(0);

    ByteBuffer bbTexCoords =
        ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * Float.BYTES);
    bbTexCoords.order(ByteOrder.nativeOrder());
    mQuadTexCoord = bbTexCoords.asFloatBuffer();
    mQuadTexCoord.put(QUAD_TEXCOORDS);
    mQuadTexCoord.position(0);

    ByteBuffer bbTexCoordsTransformed =
        ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * Float.BYTES);
    bbTexCoordsTransformed.order(ByteOrder.nativeOrder());

    int vertexShader = loadGLShader(TAG, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
    int fragmentShader = loadGLShader(TAG, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

    mQuadProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(mQuadProgram, vertexShader);
    GLES20.glAttachShader(mQuadProgram, fragmentShader);
    GLES20.glLinkProgram(mQuadProgram);
    GLES20.glUseProgram(mQuadProgram);

    ShaderUtil.checkGLError(TAG, "Program creation");

    mQuadPositionParam = GLES20.glGetAttribLocation(mQuadProgram, "a_Position");
    mQuadTexCoordParam = GLES20.glGetAttribLocation(mQuadProgram, "a_TexCoord");
    mTextureUniform = GLES20.glGetUniformLocation(mQuadProgram, "u_Texture");
    mModelViewProjectionUniform =
        GLES20.glGetUniformLocation(mQuadProgram, "u_ModelViewProjection");

    ShaderUtil.checkGLError(TAG, "Program parameters");

    Matrix.setIdentityM(mModelMatrix, 0);
  }

  private int loadGLShader(String tag, int type, String source) {
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, source);
    GLES20.glCompileShader(shader);

    // Get the compilation status.
    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    // If the compilation failed, delete the shader.
    if (compileStatus[0] == 0) {
      Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
      GLES20.glDeleteShader(shader);
      shader = 0;
    }

    if (shader == 0) {
      throw new RuntimeException("Error creating shader.");
    }

    return shader;
  }

  public void updateModelMatrix(float[] modelMatrix, float scaleFactor) {
    float[] scaleMatrix = new float[16];
    Matrix.setIdentityM(scaleMatrix, 0);
    scaleMatrix[0] = scaleFactor;
    scaleMatrix[5] = scaleFactor;
    scaleMatrix[10] = scaleFactor;
    Matrix.multiplyMM(mModelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);
  }

  public void draw(float[] cameraView, float[] cameraPerspective) {
    ShaderUtil.checkGLError(TAG, "Before draw");
    Matrix.multiplyMM(mModelViewMatrix, 0, cameraView, 0, mModelMatrix, 0);
    Matrix.multiplyMM(mModelViewProjectionMatrix, 0, cameraPerspective, 0, mModelViewMatrix, 0);

    GLES20.glUseProgram(mQuadProgram);

    // Attach the object texture.
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
    GLES20.glUniform1i(mTextureUniform, 0);
    GLES20.glUniformMatrix4fv(mModelViewProjectionUniform, 1, false, mModelViewProjectionMatrix, 0);
    // Set the vertex positions.
    GLES20.glVertexAttribPointer(
        mQuadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mQuadVertices);

    // Set the texture coordinates.
    GLES20.glVertexAttribPointer(
        mQuadTexCoordParam, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mQuadTexCoord);

    // Enable vertex arrays
    GLES20.glEnableVertexAttribArray(mQuadPositionParam);
    GLES20.glEnableVertexAttribArray(mQuadTexCoordParam);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    // Disable vertex arrays
    GLES20.glDisableVertexAttribArray(mQuadPositionParam);
    GLES20.glDisableVertexAttribArray(mQuadTexCoordParam);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    ShaderUtil.checkGLError(TAG, "After draw");
  }
}
