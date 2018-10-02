package com.belfortdev.hurbchallenge.list.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.belfortdev.hurbchallenge.R
import com.belfortdev.hurbchallenge.core.model.SearchDomain
import com.squareup.picasso.Picasso
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import kotlinx.android.synthetic.main.hotel_item.view.*
import kotlinx.android.synthetic.main.rated_section_header.view.*

class ListAdapter(private val picasso: Picasso) : SectionedRecyclerViewAdapter() {

    private var fullData = emptyList<SearchDomain.Hotel>()
    private var sectionMap = HashMap<Int, Section>()

    private enum class SortType {
        RATING
    }

    companion object {
        private const val MAX_RATING = 5
        private const val PACKAGE_SECTION = -1
    }

    init {
        buildRatedSections()
        buildPackageSection()
    }

    fun setData(data: List<SearchDomain.Hotel>?) {
        this.fullData = data ?: this.fullData
        populateRatedSections()
    }

    private fun buildRatedSections() {
        for (i in 1..MAX_RATING) {
            val ratedSection = Section(picasso, i.toFloat())
            ratedSection.isVisible = false
            addSection(ratedSection)
            sectionMap[i] = ratedSection
        }
    }

    private fun buildPackageSection() {
        val packageSection = Section(picasso, isHotel = false)
        packageSection.isVisible = false
        addSection(packageSection)
        sectionMap[PACKAGE_SECTION] = packageSection
    }

    private fun populateRatedSections() {
        for (i in 1..MAX_RATING) {
            val sectionHotels = getHotelsWithRating(i.toFloat())
            if (sectionHotels.isNotEmpty()) {
                sectionMap[i]?.addData(sectionHotels)
            }
        }
        sectionMap[PACKAGE_SECTION]?.addData(getPackages())
    }

    private fun getHotelsWithRating(numberOfStars: Float): List<SearchDomain.Hotel> {
        return fullData.filter { hotel ->
            (hotel.stars == numberOfStars) && (hotel.isHotel ?: false)
        }
    }

    private fun getPackages(): List<SearchDomain.Hotel> {
        return fullData.filter { it.isPackage ?: false }
    }

    private inner class Section(private val picasso: Picasso, private val rating: Float = 0f, private val isHotel: Boolean = true)
        : StatelessSection(SectionParameters.builder()
            .itemResourceId(R.layout.hotel_item)
            .headerResourceId(if (isHotel) R.layout.rated_section_header else R.layout.package_section_header)
            .build()) {

        var sectionData: List<SearchDomain.Hotel> = emptyList()

        fun addData(data: List<SearchDomain.Hotel>) {
            sectionData = data
            isVisible = true
            notifyDataSetChanged()
        }

        override fun getContentItemsTotal(): Int {
            return sectionData?.size
        }

        override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
            return ListItemViewHolder(view)
        }

        override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemHolder = holder as ListItemViewHolder
            itemHolder.bind(sectionData[position], picasso)
        }

        override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
            return HeaderViewHolder(view)
        }

        override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
            super.onBindHeaderViewHolder(holder)
            val headerHolder = holder as HeaderViewHolder
            headerHolder.bind(isHotel, rating)
        }
    }

    private inner class HeaderViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(isHotel: Boolean, rating: Float) {
            if (isHotel) {
                itemView.sectionRatingBar.rating = rating
            }
        }
    }

    private inner class ListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(hotel: SearchDomain.Hotel, picasso: Picasso) {
            picasso.load(hotel.image)
                    .placeholder(R.drawable.placeholder)
                    .into(itemView.hotelIV)

            with(itemView) {
                titleTV.text = hotel.name
                cityTV.text = hotel.address?.city
                setPriceTextView(hotel)
                hotel.stars?.let { ratingBar.rating = it }
                setFreeCancellationTextView(hotel)
            }
        }

        private fun View.setFreeCancellationTextView(hotel: SearchDomain.Hotel): Unit? {
            return hotel.huFreeCancellation?.let { freeCancellation ->
                if (freeCancellation) {
                    freeCancellationTV.visibility = VISIBLE
                }
            }
        }

        private fun View.setPriceTextView(hotel: SearchDomain.Hotel) {
            if (hotel.price?.currentPrice == null) {
                priceTV.text = ""
                currencyTV.visibility = GONE
            } else {
                priceTV.text = hotel.price?.currentPrice?.toInt().toString()
            }
        }
    }
}
