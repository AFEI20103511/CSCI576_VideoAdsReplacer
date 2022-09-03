from __future__ import division
from mp4_converter import convertToMP4
import subprocess
import csv
import sys



# function to get fps of src_video

def get_input_fps(src_video):
    # uses get-video-properties to get fps of input .mp4
    # augs: input mp4 path

    from videoprops import get_video_properties

    props = get_video_properties(src_video)
    tempFPS = props['avg_frame_rate']
    fps = float(tempFPS.split('/')[0]) / float(tempFPS.split('/')[1])
    # print(fps)
    return fps


# function to get breakpoints of shots, given src_video
def extract_shots_with_ffprobe(src_video, threshold=0):
    """
    uses ffprobe to produce a list of shot
    boundaries (in seconds)

    Args:
        src_video (string): the path to the source
            video
        threshold (float): the minimum value used
            by ffprobe to classify a shot boundary

    Returns:
        List[(float, float)]: a list of tuples of floats
        representing predicted shot boundaries (in seconds) and
        their associated scores
    """
    scene_ps = subprocess.Popen(("ffprobe",
                                 "-show_frames",
                                 "-of",
                                 "compact=p=0",
                                 "-f",
                                 "lavfi",
                                 "movie=" + src_video + ",select=gt(scene\," + str(threshold) + ")"),
                                stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT)
    # get fps
    fps = get_input_fps(src_video)
    output = scene_ps.stdout.read()
    boundaries = [1]
    timestamps = [0]
    # print(type(output.decode("utf-8")))
    text = output.decode("utf-8")
    # print(text)
    for line in text.split("\n"):
        if line[0:16] == 'media_type=video':
            boundary = float(line.split('|')[4].split('=')[1])
            frameNo = round(fps * boundary)
            timestamps.append(boundary)
            boundaries.append(frameNo)
    duration = 5 * 60
    lastFrame = duration * fps
    timestamps.append(duration)
    boundaries.append(lastFrame)
    print(timestamps)
    print(boundaries)

    # find the ad start frame
    numOfPoints = len(timestamps)
    tempPos = 1
    adShots = []
    adPos = []

    while tempPos < numOfPoints:
        if timestamps[tempPos] - timestamps[tempPos-1] < 12:
            shot = [timestamps[tempPos-1], timestamps[tempPos]]
            pos = [boundaries[tempPos-1], boundaries[tempPos]]
            if len(adShots) == 0 or adShots[-1][-1] != shot[0]:
                adShots.append(shot)
                adPos.append(pos)
            else:
                adShots[-1][1] = shot[1]
                adPos[-1][1] = pos[1]
        tempPos = tempPos + 1

    print(adShots)
    print(adPos)

    # output breakpoints to csv
    f = open('breakpoints', 'w')
    writer = csv.writer(f)
    writer.writerow(timestamps)
    writer.writerow(boundaries)
    writer.writerow(adShots)
    writer.writerow(adPos)
    f.close


# run main method
# args: src_video and threshold, ranging from 0 to 1.
# 0.4 produces best result.
# convertToMP4("data_test2.rgb", "output2.mp4")
# extract_shots_with_ffprobe("output2.mp4", 0.35)
if __name__ == "__main__":
    print(sys.argv[1])
    convertToMP4(sys.argv[1], "temp.mp4")
    extract_shots_with_ffprobe("temp.mp4", 0.35)

