/*
 * Copyright (C) 2016 eschao <esc.chao@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eschao.android.widget.pageflip;

import android.content.Context;

import static android.opengl.GLES20.glGetUniformLocation;

/**
 * FoldBack vertex shader program which is used to load:
 * <ul>
 *     <li>fold_back_fragment_shader.glsl</li>
 *     <li>fold_back_vertex_shader.glsl</li>
 * </ul>
 *
 * @author eschao
 */

public class FoldBackVertexProgram extends VertexProgram {

    final static String VAR_TEXTRUE_OFFSET = "u_texXOffset";
    final static String VAR_MASK_COLOR     = "u_maskColor";
    final static String VAR_SHADOW_TEXTURE = "u_shadow";

    int mShadowLoc;
    int mMaskColorLoc;
    int mTexXOffsetLoc;

    public FoldBackVertexProgram() {
        super();

        mShadowLoc = INVALID_GL_HANDLE;
        mMaskColorLoc = INVALID_GL_HANDLE;
        mTexXOffsetLoc = INVALID_GL_HANDLE;
    }

    /**
     * Initiate shader program
     *
     * @param context Android app context
     * @return self
     * @throws PageFlipException if fail to read and compile shader scripts
     */
    public FoldBackVertexProgram init(Context context) throws
                                                       PageFlipException {
        super.init(context,
                   R.raw.fold_back_vertex_shader,
                   R.raw.fold_back_fragment_shader);
        return this;
    }

    /**
     * Get variable handles defined in shader script
     */
    protected void getVarsLocation() {
        super.getVarsLocation();

        if (mProgramRef != 0) {
            mShadowLoc = glGetUniformLocation(mProgramRef, VAR_SHADOW_TEXTURE);
            mMaskColorLoc = glGetUniformLocation(mProgramRef, VAR_MASK_COLOR);
            mTexXOffsetLoc = glGetUniformLocation(mProgramRef,
                                                  VAR_TEXTRUE_OFFSET);
        }
    }

    /**
     * Delete all handles
     */
    public void delete() {
        super.delete();

        mShadowLoc = INVALID_GL_HANDLE;
        mMaskColorLoc = INVALID_GL_HANDLE;
        mTexXOffsetLoc = INVALID_GL_HANDLE;
    }
}
