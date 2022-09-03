import sys
import io
import numpy as np
from PIL import Image
import cv2 as cv

"""
open a file and returns file (remember to close), and a BufferedReader (for getFrame and getRawFrames)
"""
def openRGB(sourcefile):
    file = open(sourcefile, 'rb')
    fi  = io.FileIO(file.fileno())
    fb = io.BufferedReader(fi)
    return file, fb

"""
openRGB first, pass in BufferedReader, give frame number (starting at 0) -> get matrix of pixels
"""
def getFrame(fb, timestamp):
    fb.seek(480 * 270 * 3 * timestamp)
    raw = fb.read(480 * 270 * 3)
    r = raw[:480 * 270]
    g = raw[480 * 270:480 * 270 * 2]
    b = raw[480 * 270 * 2:]
    
    return np.array(list(zip(r, g, b))).astype(np.uint8).reshape((270, 480, 3))

"""
gets the continuous N frames starting at timestamp
"""
def getRawFrames(fb, timestamp, frame_count=1):
    fb.seek(480 * 270 * 3 * timestamp)
    return fb.read(480 * 270 * 3 * frame_count)
    
"""
turns array into a Pillow.Image object for viewing
"""
def displayRGB(rgb_arr):
    return Image.fromarray(rgb_arr)

logo_lookup = {'dataset1': {'subway': 'logo_sub_org.png', 'starbucks': 'logo_str_org.png'}, \
               'dataset2': {'nfl': 'logo_nfl_org.png', 'mcdanks': 'logo_mcd_org.png'}, \
               'dataset3': {'americaneagle': 'logo_ame_org.png', 'hardrock': 'logo_hrd_org.png'}, \
               'test2': {'subway': 'logo_sub_org.png', 'starbucks': 'logo_str_org.png'} \
              }

def detect(img_gray, dataset, logo, img_out, threshold=0.6):
    template = cv.imread(logo_lookup[dataset][logo], 0)
    w, h = template.shape[::-1]
    res = cv.matchTemplate(img_gray, template, cv.TM_CCOEFF_NORMED)
    loc = np.where(res >= threshold)
        
#     for pt in zip(*loc[::-1]):
    if  len(loc[0]) == 0:
        return False
    
    pt = (loc[1][-1], loc[0][-1])
    cv.rectangle(img_out, pt, (pt[0] + w, pt[1] + h), (0, 255, 255), 2)

    font                   = cv.FONT_HERSHEY_PLAIN
    bottomLeftCornerOfText = pt
    fontScale              = 1
    fontColor              = (255,255,255)
    thickness              = 1
    lineType               = 2

    score = str(round(res[pt[1]][pt[0]], 2))
    cv.putText(img_out, f'{logo} {score}', 
        bottomLeftCornerOfText, 
        font, 
        fontScale,
        fontColor,
        thickness,
        lineType)
    
    return True

"""
arg1: what dataset this is (ex. dataset1)
arg2: path to original 5-min RGB file (ex. dataset3/Videos/data_test3.rgb)
arg3: path to input file of ads interval (ex. advsinterval.txt)
"""
if __name__ == '__main__':
    dataset = sys.argv[1]
    sourcefile = sys.argv[2]
    file, bufreader = openRGB(sourcefile)
    print("Logo detector running......")
    with open(sys.argv[3], 'r') as f:
        ads_raw = f.read()

    ads = [[int(j) for j in i.strip("[]").split(", ")] for i in ads_raw[1:-2].split('","')]

    rgb_seq = []
    logos = list(logo_lookup[dataset].keys())
    logo1 = logos[0]
    logo2 = logos[1]
    
    for ad_i in range(len(ads)):
        if ads[ad_i][1] < 500:
            continue # remove the ads

        ads_detected = set()
        ads_seq = []
        
        with open(f'newrgb{ad_i+1}.rgb', 'ab') as ofile:
            detect_from = 0 if ad_i == 0 else ads[ad_i-1][1]
            detect_to = ads[ad_i][0]

            print(detect_from, detect_to)

            for frame in range(detect_from, detect_to, 1):
                
                img_rgb = getFrame(bufreader, frame)
                img_out = getFrame(bufreader, frame)
                img_gray = cv.cvtColor(img_rgb, cv.COLOR_BGR2GRAY)
                found1 = detect(img_gray, dataset, logo1, img_out, 0.6)
                found2 = detect(img_gray, dataset, logo2, img_out, 0.6)

                if found1:
                    print(f'found {logo1} at {frame}')
                    if logo1 not in ads_detected:
                        ads_detected.add(logo1)
                        ads_seq.append(logo1)
                        
                if found2:
                    print(f'found {logo2} at {frame}')
                    if logo2 not in ads_detected:
                        ads_detected.add(logo2)
                        ads_seq.append(logo2)
                        
                r = img_out[:,:,0].flatten()
                g = img_out[:,:,1].flatten()
                b = img_out[:,:,2].flatten()

                ofile.write(bytes(np.concatenate([r, g, b]).tolist()))  
        
        rgb_seq.append(f'newrgb{ad_i+1}.rgb')
        rgb_seq += list(ads_detected)
        
    file.close()

    with open(f'{dataset}_output.txt', 'w') as f:
        f.write(' '.join(rgb_seq))