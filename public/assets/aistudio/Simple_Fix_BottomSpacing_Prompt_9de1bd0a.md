Fix the excessive bottom spacing in the Video Lectures player screen of the Bayan Al-Huda app.

Current problem: Large empty white space below the action buttons (YT Sign-In, Landscape, YouTube) and above the bottom navigation bar.

Requirements:
1. Root layout must use match_parent for height
2. Remove any large bottom padding or margin (>24dp) from all views in this screen
3. Bottom navigation must sit at the very bottom edge of the screen
4. Video player must keep 16:9 aspect ratio
5. Keep all existing buttons and functionality working (360p quality, Landscape mode, YT Sign-In, YouTube button, bottom nav tabs)
6. Only modify the video player screen layout XML, do not change other screens
7. Use ConstraintLayout with proper constraints to eliminate empty space

Provide the corrected layout XML file.