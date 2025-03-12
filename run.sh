clear
gradle clean build
gradle run --no-build-cache --rerun-tasks \
          --args="--matrix-size=40 \
                  --output-directory=./example \
                  --number-of-particles=3000 \
                  --board-side-length=20 \
                  --interaction-radius=1 \
                  --particle-radius=0.25 \
                  --generate-random \
                  --algorithm=BRUTE_FORCE"
gradle run --no-build-cache --rerun-tasks \
          --args="--matrix-size=40 \
                  --output-directory=./example \
                  --number-of-particles=3000 \
                  --board-side-length=20 \
                  --interaction-radius=1 \
                  --particle-radius=0.25 \
                  --generate-random \
                  --algorithm=CELL_INDEX_METHOD"
