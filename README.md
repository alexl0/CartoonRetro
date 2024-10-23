# [Cartoon Retro](twitch.tv/cartoonretro)
I just don't want my young cousins to see Pepa Pig so I automated a Twitch channel that shows old cartoons.

The channel is automated by a Java Maven project.
It's connected to:
  - The twitch API: to show info about the current show being streamed (like number of episode, etc).
  - OBS Studio Java API: to broadcast the shows.
  - A HDD drive with a lot of show from the 90's.
  - A mysql DB with a lot of metadata of each episode.

The project runs on an Ubuntu virtual machine and only works on VMWare Workstation due to VirtualBox GPU limitations. 

[twitch.tv/cartoonretro](twitch.tv/cartoonretro)
