# FancyPhotoPicker
This project show hot to use RecyclerViewAdapter from Android SupportLibrary to show preview of photos stored in device memory or sd card.

## Features:
  - Loading scaled down preview of photos - loading and scaling is done asynchronously, I  also used WeakReference to View that actually shows photo
  so it doesn't prevent garnage collector from recycling bitmap wneh Recycler's view child is detached. It greatly improvs performance.
  - Single click, and multiple chooice after long tap options are supported.
