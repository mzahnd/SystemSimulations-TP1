matrix_size=10
output_directory=./example
number_of_particles=300
board_side_length=20
interaction_radius=1
particle_radius=0.25
periodic_contour=false

clear
gradle clean build
gradle run --no-build-cache --rerun-tasks \
          --args="--matrix-size=${matrix_size} \
                  --output-directory=${output_directory} \
                  --number-of-particles=${number_of_particles} \
                  --board-side-length=${board_side_length} \
                  --interaction-radius=${interaction_radius} \
                  --particle-radius=${particle_radius} \
                  --periodic-contour=${periodic_contour} \
                  --generate-random"
pipenv run python ./src/main/python/animate_particles.py \
                      --matrix_size ${matrix_size}\
                      --input_directory ${output_directory}\
                      --number_of_particles ${number_of_particles}\
                      --board_side_length ${board_side_length}\
                      --interaction_radius ${interaction_radius}\
                      --particle_radius ${particle_radius}
pipenv run python ./src/main/python/animate_particles.py \
                      --matrix_size ${matrix_size}\
                      --input_directory ${output_directory}\
                      --number_of_particles ${number_of_particles}\
                      --board_side_length ${board_side_length}\
                      --interaction_radius ${interaction_radius}\
                      --particle_radius ${particle_radius} \
                      --brute_force