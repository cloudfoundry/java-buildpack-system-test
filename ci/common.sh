ROOT=$(realpath "$(dirname "${BASH_SOURCE[0]}")"/../..)

if [[ -d "${PWD}"/maven && ! -d "${HOME}"/.m2 ]]; then
  printf "➜ Linking Maven cache\n"
  ln -s "${PWD}"/maven "${HOME}"/.m2
fi


if [[ -d "${ROOT}"/om ]]; then
  printf "➜ Preparing om\n"
  mv "${ROOT}"/om/om-linux-* "${ROOT}"/om/om
  chmod +x "${ROOT}"/om/om
  export PATH="${ROOT}"/om:${PATH}
fi

