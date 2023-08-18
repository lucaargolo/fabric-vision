package io.github.lucaargolo.fabricvision.client

import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.resource.ResourceManager

class FramebufferTexture(private val framebuffer: Framebuffer, private val depth: Boolean): AbstractTexture() {

    override fun getGlId(): Int {
        return if(depth) framebuffer.depthAttachment else framebuffer.colorAttachment
    }

    override fun clearGlId() {

    }

    override fun load(manager: ResourceManager) {
    }
}