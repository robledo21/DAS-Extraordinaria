// PhotoAdapter.kt
package com.example.extraordinariadas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import android.content.Context
import android.widget.BaseAdapter

class PhotoAdapter(private val context: Context, private var photoUrls: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return photoUrls.size
    }

    override fun getItem(position: Int): Any {
        return photoUrls[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item_photo, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Load image into ImageView using Picasso
        Picasso.get().load(photoUrls[position]).into(viewHolder.imageView)

        return view
    }

    fun updateData(newPhotoUrls: List<String>) {
        photoUrls = newPhotoUrls
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
}
