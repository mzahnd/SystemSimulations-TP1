matrix_size=40
output_directory=./example
number_of_particles=3000
board_side_length=20
interaction_radius=1
particle_radius=0.25
periodic_countour=false

clear
gradle clean build
gradle run --no-build-cache --rerun-tasks \
          --args="--matrix-size=${matrix_size} \
                  --output-directory=${output_directory} \
                  --number-of-particles=${number_of_particles} \
                  --board-side-length=${board_side_length} \
                  --interaction-radius=${interaction_radius} \
                  --particle-radius=${particle_radius} \
                  --periodic-countour=${periodic_countour} \
                  --generate-random \
                  --algorithm=BRUTE_FORCE"
gradle run --no-build-cache --rerun-tasks \
          --args="--matrix-size=${matrix_size} \
                  --output-directory=${output_directory} \
                  --number-of-particles=${number_of_particles} \
                  --board-side-length=${board_side_length} \
                  --interaction-radius=${interaction_radius} \
                  --particle-radius=${particle_radius} \
                  --periodic-countour=${periodic_countour} \
                  --generate-random \
                  --algorithm=CELL_INDEX_METHOD"
