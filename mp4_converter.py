import subprocess


def convertToMP4(src_video, output_video):
    temp = subprocess.Popen(("ffmpeg",
                      "-s",
                      "480x270",
                      "-r",
                      "30",
                      "-pix_fmt",
                      "gbrp",
                      "-i",
                      src_video,
                      "-c:v",
                      "libx264",
                      "-y",
                      output_video),
                      stdout=subprocess.PIPE,
                      stderr=subprocess.STDOUT)

    out = temp.stdout.read().decode("utf-8")
    outputname = str(output_video)
    print(out)
    return outputname
    # return output_video




