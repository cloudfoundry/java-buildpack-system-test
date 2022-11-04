ROOT=$(realpath "$(dirname "${BASH_SOURCE[0]}")"/../..)

if [[ -d "${PWD}"/maven && ! -d "${HOME}"/.m2 ]]; then
  printf "➜ Linking Maven cache\n"
  ln -s "${PWD}"/maven "${HOME}"/.m2
fi

if [[ -d "${ROOT}"/om ]]; then
  printf "➜ Expanding om\n"
  tar xzf "${ROOT}"/om/om-linux-amd64-*.tar.gz -C "${ROOT}"/om
  export PATH="${ROOT}"/om:${PATH}
fi
